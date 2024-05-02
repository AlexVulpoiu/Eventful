package com.unibuc.fmi.eventful.model;

import com.unibuc.fmi.eventful.enums.EventStatus;
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
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private LocalDateTime dateTime;

    @Enumerated(value = EnumType.STRING)
    private EventStatus status;

    @ManyToOne
    private AbstractLocation location;

    @ManyToOne
    private CharitableCause charitableCause;

    @ManyToOne
    private Organiser organiser;
}
