package com.unibuc.fmi.eventful.model;

import com.unibuc.fmi.eventful.exceptions.NotFoundException;
import com.unibuc.fmi.eventful.model.ids.StandingCategoryId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "standing_categories")
public class StandingCategory {

    @EmbeddedId
    private StandingCategoryId id;

    private int capacity;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("standingLocationId")
    private StandingLocation standingLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("eventId")
    private Event event;

    @OneToMany(mappedBy = "standingCategory", fetch = FetchType.EAGER)
    private List<TicketPhase> ticketPhases;

    public StandingCategory(StandingCategoryId id, int capacity, StandingLocation standingLocation, Event event) {
        this.id = id;
        this.capacity = capacity;
        this.standingLocation = standingLocation;
        this.event = event;
    }

    public TicketPhase getCurrentTicketPhase() {
        return ticketPhases.stream().filter(ticketPhase -> ticketPhase.getDateLimit().isAfter(LocalDate.now()))
                .min(Comparator.comparing(TicketPhase::getDateLimit))
                .orElseThrow(() -> new NotFoundException("No selling phase available at the moment!"));
    }

    public int getSoldTickets() {
        return ticketPhases.stream().map(ticketPhase -> ticketPhase.getStandingTickets().size()).reduce(0, Integer::sum);
    }
}
