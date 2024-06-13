package com.unibuc.fmi.eventful.model;

import com.unibuc.fmi.eventful.model.ids.CategoryPriceId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "categories_prices")
public class CategoryPrice {

    @EmbeddedId
    private CategoryPriceId id;

    private double price;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("categoryId")
    private SeatsCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("eventId")
    private Event event;

    public double getCurrentPrice() {
        var discount = 0;
        if (event.getActivePromotion().isPresent()) {
            discount = event.getActivePromotion().get().getValue();
        }

        return (100 - discount) * price / 100;
    }
}
