package com.unibuc.fmi.eventful.model;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
@Table(name = "users")
@EqualsAndHashCode(callSuper = true)
public class User extends AbstractUser {

    private int xp = 0;

    private int availablePoints = 0;

    @OneToMany(mappedBy = "user")
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Raffle> raffles = new ArrayList<>();

    public User(String firstName, String lastName, String email, String password) {
        super(firstName, lastName, email, password);
        this.xp = 100;
        this.availablePoints = 100;
    }

    public void addPoints(int points) {
        this.xp += points;
        this.availablePoints += points;
    }

    public void usePoints(int points) {
        this.availablePoints -= points;
    }
}
