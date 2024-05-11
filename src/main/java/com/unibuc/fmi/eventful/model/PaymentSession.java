package com.unibuc.fmi.eventful.model;

import com.unibuc.fmi.eventful.enums.PaymentSessionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payment_sessions")
public class PaymentSession {

    @Id
    private String id;

    @Enumerated(value = EnumType.STRING)
    private PaymentSessionStatus paymentStatus;

    @OneToOne
    private PaymentIntent paymentIntent;

    public PaymentSession(String id) {
        this.id = id;
    }

    public void setPaymentStatusFromStripeStatus(String stripeStatus) {
        this.paymentStatus = switch (stripeStatus) {
            case "complete" -> PaymentSessionStatus.COMPLETE;
            case "expired" -> PaymentSessionStatus.EXPIRED;
            case "open" -> PaymentSessionStatus.OPEN;
            default -> null;
        };
    }
}
