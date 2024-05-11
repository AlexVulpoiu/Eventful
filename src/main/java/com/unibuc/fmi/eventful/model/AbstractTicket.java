package com.unibuc.fmi.eventful.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

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

    protected UUID externalId;

    protected boolean validated;

    protected AbstractTicket() {
        this.externalId = UUID.randomUUID();
        this.validated = false;
    }
}
