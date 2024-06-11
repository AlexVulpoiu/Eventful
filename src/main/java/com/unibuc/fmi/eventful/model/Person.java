package com.unibuc.fmi.eventful.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "persons")
@EqualsAndHashCode(callSuper = true)
public class Person extends Organiser {

    private Long cnp;

    @Override
    public String getOrganiserName() {
        return getFullName();
    }
}
