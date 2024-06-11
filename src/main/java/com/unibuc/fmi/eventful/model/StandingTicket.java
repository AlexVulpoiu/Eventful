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
    private StandingCategory standingCategory;

    protected StandingTicket() {

    }

    public StandingTicket(Order order, StandingCategory standingCategory) {
        super(order);
        this.standingCategory = standingCategory;
    }

    @Override
    public String getName() {
        return String.format("Category %s", standingCategory.getId().getName());
    }
}
