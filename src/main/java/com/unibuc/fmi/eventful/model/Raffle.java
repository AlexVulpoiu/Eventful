package com.unibuc.fmi.eventful.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "raffles")
public class Raffle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int participantsLimit;

    private LocalDate endDate;

    private int prize;

    private String partnerName;

    private int totalParticipants;

    @ManyToOne
    private User user;

    @OneToOne
    private Event event;

    @OneToOne
    private Voucher voucher;

    public void increaseTotalParticipants() {
        totalParticipants++;
    }
}
