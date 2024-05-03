package com.unibuc.fmi.eventful.model;

import com.unibuc.fmi.eventful.model.ids.TicketPhaseId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tickets_phases")
public class TicketPhase {

    @EmbeddedId
    private TicketPhaseId id;

    private double price;

    private LocalDate dateLimit;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("standingLocationId")
    private StandingLocation standingLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("eventId")
    private Event event;
}
