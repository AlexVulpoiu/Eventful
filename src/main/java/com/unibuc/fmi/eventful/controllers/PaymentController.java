package com.unibuc.fmi.eventful.controllers;

import com.stripe.exception.StripeException;
import com.unibuc.fmi.eventful.dto.request.payment.PaymentRequest;
import com.unibuc.fmi.eventful.dto.response.payment.PaymentResponse;
import com.unibuc.fmi.eventful.security.services.UserDetailsImpl;
import com.unibuc.fmi.eventful.services.PaymentService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payment")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {

    PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasAuthority('USER')")
    public PaymentResponse initiatePayment(@Valid @RequestBody PaymentRequest paymentRequest,
                                           @AuthenticationPrincipal UserDetailsImpl principal) {
        return paymentService.initiatePayment(paymentRequest, principal.getId());
    }

    @PostMapping("/webhook")
    public void handleWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String signatureHeader)
            throws StripeException {
        paymentService.handleWebhook(payload, signatureHeader);
    }
}
