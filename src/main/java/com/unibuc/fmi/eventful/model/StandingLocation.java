package com.unibuc.fmi.eventful.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@Table(name = "standing_locations")
@EqualsAndHashCode(callSuper = true)
public class StandingLocation extends AbstractLocation {

    private int capacity;
}
