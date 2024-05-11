package com.unibuc.fmi.eventful.model;

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
@Table(name = "users")
@EqualsAndHashCode(callSuper = true)
public class User extends AbstractUser {

    @OneToMany(mappedBy = "user")
    private List<Order> orders;

    public User(String firstName, String lastName, String email, String password) {
        super(firstName, lastName, email, password);
    }
}
