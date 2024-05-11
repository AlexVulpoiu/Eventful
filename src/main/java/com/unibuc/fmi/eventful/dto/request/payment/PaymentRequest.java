package com.unibuc.fmi.eventful.dto.request.payment;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    private long orderId;

    private long amount;

    private String eventName;

    @NotNull
    private LocalDateTime orderDate;

    public String getOrderName() {
        return "Eventful\nOrder #" + orderId + " from " + DateTimeFormatter.ofPattern("dd/MM/yyyy").format(orderDate) + " for " + eventName;
    }
}
