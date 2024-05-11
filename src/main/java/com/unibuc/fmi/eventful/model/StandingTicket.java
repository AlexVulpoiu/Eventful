package com.unibuc.fmi.eventful.model;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@Table(name = "standing_tickets")
@EqualsAndHashCode(callSuper = true)
public class StandingTicket extends AbstractTicket {

    @ManyToOne
    private TicketPhase ticketPhase;

    public StandingTicket(TicketPhase ticketPhase) {
        super();
        this.ticketPhase = ticketPhase;
    }
}
