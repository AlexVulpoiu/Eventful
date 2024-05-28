package com.unibuc.fmi.eventful.model;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@Entity
@SuperBuilder
@Table(name = "standing_tickets")
@EqualsAndHashCode(callSuper = true)
public class StandingTicket extends AbstractTicket {

    @ManyToOne
    private TicketPhase ticketPhase;

    protected StandingTicket() {

    }

    public StandingTicket(Order order, TicketPhase ticketPhase) {
        super(order);
        this.ticketPhase = ticketPhase;
    }

    @Override
    public String getName() {
        return String.format("Category %s, %s selling phase", ticketPhase.getStandingCategory().getId().getName(), ticketPhase.getName());
    }
}
