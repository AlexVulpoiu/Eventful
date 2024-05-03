package com.unibuc.fmi.eventful.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

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

    @OneToOne(cascade = CascadeType.ALL)
    protected Address address;

    @OneToOne(cascade = CascadeType.ALL)
    protected BankAccount bankAccount;

    @OneToMany(mappedBy = "organiser")
    protected List<Event> events;

    @OneToMany(mappedBy = "organiser")
    protected List<CharitableCause> charitableCauses;
}
