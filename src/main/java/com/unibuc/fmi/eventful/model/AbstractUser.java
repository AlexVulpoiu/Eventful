package com.unibuc.fmi.eventful.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "abstract_users")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AbstractUser {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    protected Long id;

    protected String firstName;

    protected String lastName;

    protected String email;

    protected String password;

    protected String phone;

    protected UUID verificationCode;

    protected boolean enabled;

    @ManyToMany
    @JoinTable(name = "users_roles", joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    protected Set<Role> roles;

    @OneToMany(mappedBy = "abstractUser", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    protected List<Feedback> feedbackList = new ArrayList<>();

    public String getFullName() {
        return String.join(" ", firstName, lastName);
    }

    protected AbstractUser(String firstName, String lastName, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.verificationCode = UUID.randomUUID();
        this.enabled = false;
    }
}
