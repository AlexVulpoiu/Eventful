package com.unibuc.fmi.eventful.model;

import com.unibuc.fmi.eventful.enums.ChargeStatus;
import com.unibuc.fmi.eventful.enums.PaymentIntentStatus;
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
@Table(name = "payment_intents")
public class PaymentIntent {

    @Id
    private String id;

    @Enumerated(value = EnumType.STRING)
    private PaymentIntentStatus intentStatus;

    @Enumerated(value = EnumType.STRING)
    private ChargeStatus chargeStatus;

    public PaymentIntent(String id) {
        this.id = id;
    }

    public void setChargeStatusFromStripeStatus(String stripeStatus) {
        this.chargeStatus = switch (stripeStatus) {
            case "succeeded" -> ChargeStatus.SUCCEEDED;
            case "pending" -> ChargeStatus.PENDING;
            case "failed" -> ChargeStatus.FAILED;
            default -> null;
        };
    }

    public void setIntentStatusFromStripeStatus(String stripeStatus) {
        this.intentStatus = switch (stripeStatus) {
            case "canceled" -> PaymentIntentStatus.CANCELED;
            case "processing" -> PaymentIntentStatus.PROCESSING;
            case "succeeded" -> PaymentIntentStatus.SUCCEEDED;
            default -> PaymentIntentStatus.CREATED;
        };
    }
}
