package com.unibuc.fmi.eventful.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "charitable_causes")
public class CharitableCause {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private int neededAmount;

    private double collectedAmount;

    private LocalDateTime updatedAt;

    @ManyToOne
    private Organiser organiser;
}
