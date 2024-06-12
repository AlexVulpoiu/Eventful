package com.unibuc.fmi.eventful.repository;

import com.unibuc.fmi.eventful.model.Raffle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RaffleRepository extends JpaRepository<Raffle, Long> {

    @Query("SELECT r " +
            "FROM Raffle r " +
            "WHERE r.endDate = :date")
    List<Raffle> findAllEndedAt(LocalDate date);
}
