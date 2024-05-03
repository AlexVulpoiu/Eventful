package com.unibuc.fmi.eventful.model;

import com.unibuc.fmi.eventful.model.ids.StandingCategoryId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @OneToMany(mappedBy = "standingCategory")
    private List<TicketPhase> ticketPhases;

    public StandingCategory(StandingCategoryId id, int capacity, StandingLocation standingLocation, Event event) {
        this.id = id;
        this.capacity = capacity;
        this.standingLocation = standingLocation;
        this.event = event;
    }
}
