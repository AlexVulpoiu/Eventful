package com.unibuc.fmi.eventful.controllers;

import com.google.zxing.WriterException;
import com.stripe.exception.StripeException;
import com.unibuc.fmi.eventful.dto.response.payment.PaymentResponse;
import com.unibuc.fmi.eventful.security.services.UserDetailsImpl;
import com.unibuc.fmi.eventful.services.PaymentService;
import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payment")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {

    PaymentService paymentService;

    @PostMapping("/{orderId}")
    @PreAuthorize("hasAuthority('USER')")
    public PaymentResponse initiatePayment(@PathVariable long orderId, @AuthenticationPrincipal UserDetailsImpl principal) {
        return paymentService.initiatePayment(orderId, principal.getId());
    }

    @PostMapping("/webhook")
    public void handleWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String signatureHeader)
            throws StripeException, MessagingException, IOException, WriterException {
        paymentService.handleWebhook(payload, signatureHeader);
    }
}
