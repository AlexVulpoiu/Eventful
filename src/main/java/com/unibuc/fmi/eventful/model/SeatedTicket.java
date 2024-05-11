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
@Table(name = "seated_tickets")
@EqualsAndHashCode(callSuper = true)
public class SeatedTicket extends AbstractTicket {

    private int numberOfRow;

    private int seat;

    @ManyToOne
    private CategoryPrice categoryPrice;

    public SeatedTicket(int numberOfRow, int seat, CategoryPrice categoryPrice) {
        super();
        this.numberOfRow = numberOfRow;
        this.seat = seat;
        this.categoryPrice = categoryPrice;
    }
}
