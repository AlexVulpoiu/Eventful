package com.unibuc.fmi.eventful.model;

import com.unibuc.fmi.eventful.enums.PaymentIntentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID externalId;

    private LocalDateTime orderDate;

    private double total;

    private int discountPoints;

    @ManyToOne
    private User user;

    @ManyToOne
    private Event event;

    @OneToMany(mappedBy = "order", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<AbstractTicket> tickets;

    @OneToOne
    private PaymentSession paymentSession;

    @OneToOne
    private Voucher voucher;

    public Order(User user, Event event) {
        this.user = user;
        this.event = event;
        this.orderDate = LocalDateTime.now();
        this.externalId = UUID.randomUUID();
        this.total = 0.0;
    }

    public String getName() {
        return "Eventful Order #" + id + " from " + DateTimeFormatter.ofPattern("dd/MM/yyyy").format(orderDate) + " for " + event.getName();
    }

    public double getPaymentAmount() {
        return total - 1.0 * discountPoints / 10.0;
    }

    public String getStatus() {
        if (paymentSession == null || paymentSession.getPaymentIntent() == null) {
            return String.valueOf(PaymentIntentStatus.PROCESSING);
        }

        return String.valueOf(paymentSession.getPaymentIntent().getIntentStatus());
    }
}
