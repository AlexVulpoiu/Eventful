package com.unibuc.fmi.eventful.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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

    @ManyToOne
    private User user;

    @ManyToOne
    private Event event;

    @OneToMany
    private List<AbstractTicket> tickets;

    @OneToOne
    private PaymentSession paymentSession;

    public Order(User user, Event event) {
        this.user = user;
        this.event = event;
        this.orderDate = LocalDateTime.now();
        this.externalId = UUID.randomUUID();
        this.total = 0.0;
    }
}
