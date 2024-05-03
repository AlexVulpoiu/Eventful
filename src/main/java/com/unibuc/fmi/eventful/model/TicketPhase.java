package com.unibuc.fmi.eventful.model;

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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private double price;

    private LocalDate dateLimit;

    @ManyToOne
    private StandingCategory standingCategory;

    public TicketPhase(String name, double price, LocalDate dateLimit, StandingCategory standingCategory) {
        this.name = name;
        this.price = price;
        this.dateLimit = dateLimit;
        this.standingCategory = standingCategory;
    }
}
