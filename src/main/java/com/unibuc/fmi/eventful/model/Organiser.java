package com.unibuc.fmi.eventful.model;

import com.unibuc.fmi.eventful.enums.OrganiserStatus;
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

    @Enumerated(EnumType.STRING)
    protected OrganiserStatus status;

    protected String commerceRegistrationNumber;

    @OneToOne(cascade = CascadeType.ALL)
    protected Address address;

    @OneToOne(cascade = CascadeType.ALL)
    protected BankAccount bankAccount;

    @OneToMany(mappedBy = "organiser")
    protected List<Event> events = new ArrayList<>();

    @OneToMany(mappedBy = "organiser")
    protected List<CharitableCause> charitableCauses = new ArrayList<>();

    public abstract String getOrganiserName();
}
