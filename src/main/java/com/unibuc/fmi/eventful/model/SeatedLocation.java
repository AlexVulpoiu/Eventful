package com.unibuc.fmi.eventful.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@Table(name = "seated_locations")
@EqualsAndHashCode(callSuper = true)
public class SeatedLocation extends AbstractLocation {

    private int numberOfRows;

    private int seatsPerRow;

    @OneToMany(mappedBy = "seatedLocation", cascade = CascadeType.ALL)
    private List<SeatsCategory> seatsCategories;
}
