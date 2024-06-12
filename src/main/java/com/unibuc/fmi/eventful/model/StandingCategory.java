package com.unibuc.fmi.eventful.model;

import com.unibuc.fmi.eventful.model.ids.StandingCategoryId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
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

    private double price;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("standingLocationId")
    private StandingLocation standingLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("eventId")
    private Event event;

    @OneToMany(mappedBy = "standingCategory", fetch = FetchType.EAGER)
    private List<StandingTicket> standingTickets = new ArrayList<>();

    public StandingCategory(StandingCategoryId id, int capacity, double price, StandingLocation standingLocation,
                            Event event) {
        this.id = id;
        this.capacity = capacity;
        this.price = price;
        this.standingLocation = standingLocation;
        this.event = event;
    }

    public int getSoldTickets() {
        return standingTickets.size();
    }

    public double getPrice() {
        var discount = 0;
        if (event.getActivePromotion().isPresent()) {
            discount = event.getActivePromotion().get().getValue();
        }

        return (100 - discount) * price / 100;
    }
}
