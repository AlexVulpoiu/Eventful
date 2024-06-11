package com.unibuc.fmi.eventful.model;

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
@Table(name = "seats_categories")
public class SeatsCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int minRow;

    private int maxRow;

    private int minSeat;

    private int maxSeat;

    @ManyToOne
    private SeatedLocation seatedLocation;
}
