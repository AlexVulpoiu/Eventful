package com.unibuc.fmi.eventful.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.RandomStringUtils;

import java.security.SecureRandom;

@Data
@Entity
@SuperBuilder
@AllArgsConstructor
@Table(name = "abstract_tickets")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AbstractTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    protected String externalId;

    protected boolean validated;

    @ManyToOne(fetch = FetchType.EAGER)
    private Order order;

    protected AbstractTicket() {

    }

    protected AbstractTicket(Order order) {
        this.externalId = RandomStringUtils.random(16, 0, 0, true, true, null, new SecureRandom());
        this.validated = false;
        this.order = order;
    }

    public abstract String getName();
}
