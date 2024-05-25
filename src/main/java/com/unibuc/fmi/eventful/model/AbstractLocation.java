package com.unibuc.fmi.eventful.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "locations")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AbstractLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    protected String name;

    protected String city;

    protected String country;

    protected String address;

    @OneToMany(mappedBy = "location")
    List<Event> events;

    public String getFullAddressDetails() {
        return String.join(", ", address, city, country);
    }

    public String getFullAddressWithName() {
        return String.join(", ", name, getFullAddressDetails());
    }
}
