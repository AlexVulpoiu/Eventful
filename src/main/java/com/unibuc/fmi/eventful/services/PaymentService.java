package com.unibuc.fmi.eventful.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import com.unibuc.fmi.eventful.dto.request.payment.PaymentRequest;
import com.unibuc.fmi.eventful.dto.response.payment.PaymentResponse;
import com.unibuc.fmi.eventful.enums.ChargeStatus;
import com.unibuc.fmi.eventful.enums.PaymentIntentStatus;
import com.unibuc.fmi.eventful.exceptions.ForbiddenException;
import com.unibuc.fmi.eventful.exceptions.NotFoundException;
import com.unibuc.fmi.eventful.exceptions.PaymentException;
import com.unibuc.fmi.eventful.model.PaymentIntent;
import com.unibuc.fmi.eventful.model.PaymentSession;
import com.unibuc.fmi.eventful.repository.OrderRepository;
import com.unibuc.fmi.eventful.repository.PaymentIntentRepository;
import com.unibuc.fmi.eventful.repository.PaymentSessionRepository;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentService {

    private static final String CURRENCY = "RON";

    @Value("${eventful.app.payment.success.url}")
    String successUrl;

    @Value("${eventful.app.payment.cancel.url}")
    String cancelUrl;

    @Value("${stripe.api.key}")
    String stripeApiKey;

    @Value("${stripe.webhook.secret}")
    String webhookSecret;

    final OrderRepository orderRepository;
    final PaymentIntentRepository paymentIntentRepository;
    final PaymentSessionRepository paymentSessionRepository;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    @Transactional
    public PaymentResponse initiatePayment(PaymentRequest paymentRequest, Long userId) {
        var order = orderRepository.findById(paymentRequest.getOrderId())
                .orElseThrow(() -> new NotFoundException("Order with id " + paymentRequest.getOrderId() + " not found!"));
        if (!order.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You are not allowed to perform this operation!");
        }

        SessionCreateParams.LineItem.PriceData.ProductData productData =
                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName(paymentRequest.getOrderName())
                        .build();

        SessionCreateParams.LineItem.PriceData priceData =
                SessionCreateParams.LineItem.PriceData.builder()
                        .setUnitAmount(paymentRequest.getAmount())
                        .setCurrency(CURRENCY)
                        .setProductData(productData)
                        .build();

        SessionCreateParams.LineItem lineItem =
                SessionCreateParams.LineItem.builder()
                        .setPriceData(priceData)
                        .setQuantity(1L)
                        .build();

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl(successUrl + paymentRequest.getOrderId())
                        .setCancelUrl(cancelUrl + paymentRequest.getOrderId())
                        .addLineItem(lineItem)
                        .build();

        Session session;
        try {
            session = Session.create(params);
        } catch (StripeException e) {
            log.error("[STRIPE] An error occurred during payment session creation.");
            throw new PaymentException("Payment session creation failed!");
        }

        var paymentSession = new PaymentSession(session.getId());
        paymentSession = paymentSessionRepository.save(paymentSession);

        order.setPaymentSession(paymentSession);
        orderRepository.save(order);

        return new PaymentResponse(session.getId(), session.getUrl());
    }

    @Transactional
    public void handleWebhook(String payload, String signatureHeader) throws StripeException {
        com.stripe.model.Event event;

        log.info("[STRIPE] Received webhook from Stripe");
        try {
            event = Webhook.constructEvent(payload, signatureHeader, webhookSecret);
        } catch (Exception e) {
            throw new RuntimeException("An error occurred when validating signature for Stripe webhook!");
        }

        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject;
        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
        } else {
            throw new RuntimeException("An error occurred when processing the Stripe webhook!");
        }

        log.info("[STRIPE] Event of type " + event.getType() + " received");
        if (List.of("charge.failed", "charge.pending", "charge.succeeded").contains(event.getType())) {
            Charge charge = (Charge) stripeObject;

            var paymentIntent = com.stripe.model.PaymentIntent.retrieve(charge.getPaymentIntent());
            var paymentIntentDb = paymentIntentRepository.findById(charge.getPaymentIntent()).orElse(
                    paymentIntentRepository.save(new PaymentIntent(charge.getPaymentIntent()))
            );
            paymentIntentDb.setChargeStatusFromStripeStatus(charge.getStatus());
            if (ChargeStatus.FAILED.equals(paymentIntentDb.getChargeStatus())) {
                paymentIntentDb.setIntentStatus(PaymentIntentStatus.PAYMENT_FAILED);
            } else {
                paymentIntentDb.setIntentStatusFromStripeStatus(paymentIntent.getStatus());
            }
            paymentIntentRepository.save(paymentIntentDb);

        } else if (List.of("checkout.session.completed", "checkout.session.expired").contains(event.getType())) {
            Session session = (Session) stripeObject;

            var stripeSession = Session.retrieve(session.getId());
            var sessionDb = paymentSessionRepository.findById(session.getId()).orElse(
                    paymentSessionRepository.save(new PaymentSession(session.getId()))
            );
            sessionDb.setPaymentStatusFromStripeStatus(stripeSession.getStatus());

            if (stripeSession.getPaymentIntent() != null) {
                var paymentIntentDb = paymentIntentRepository.findById(stripeSession.getPaymentIntent()).orElse(
                        paymentIntentRepository.save(new PaymentIntent(stripeSession.getPaymentIntent()))
                );
                var paymentIntent = com.stripe.model.PaymentIntent.retrieve(stripeSession.getPaymentIntent());
                paymentIntentDb.setIntentStatusFromStripeStatus(paymentIntent.getStatus());
                paymentIntentRepository.save(paymentIntentDb);
                if (sessionDb.getPaymentIntent() == null) {
                    sessionDb.setPaymentIntent(paymentIntentDb);
                }
            }
            paymentSessionRepository.save(sessionDb);

        } else if (List.of("payment_intent.canceled", "payment_intent.created", "payment_intent.payment_failed",
                "payment_intent.processing", "payment_intent.succeeded").contains(event.getType())) {
            com.stripe.model.PaymentIntent paymentIntent = (com.stripe.model.PaymentIntent) stripeObject;

            var stripePaymentIntent = com.stripe.model.PaymentIntent.retrieve(paymentIntent.getId());
            var paymentIntentDb = paymentIntentRepository.findById(stripePaymentIntent.getId()).orElse(
                    paymentIntentRepository.save(new PaymentIntent(stripePaymentIntent.getId()))
            );
            paymentIntentDb.setIntentStatusFromStripeStatus(stripePaymentIntent.getStatus());
            paymentIntentRepository.save(paymentIntentDb);
        }
    }
}
