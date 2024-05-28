package com.unibuc.fmi.eventful.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "organisers")
@EqualsAndHashCode(callSuper = true)
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Organiser extends AbstractUser {

    protected String commerceRegistrationNumber;

    protected double rating = 0.0;

    @OneToOne(cascade = CascadeType.ALL)
    protected Address address;

    @OneToOne(cascade = CascadeType.ALL)
    protected BankAccount bankAccount;

    @OneToMany(mappedBy = "organiser")
    protected List<Event> events = new ArrayList<>();

    @OneToMany(mappedBy = "organiser")
    protected List<CharitableCause> charitableCauses = new ArrayList<>();

    public void updateRating() {
        this.events.stream().filter(e -> e.getRating() > 0).mapToDouble(Event::getRating).average()
                .ifPresentOrElse(d -> this.rating = Math.floor(d * 100) / 100, () -> this.rating = 0.0);
    }
}
