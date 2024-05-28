package com.unibuc.fmi.eventful.repository;

import com.unibuc.fmi.eventful.model.AbstractTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AbstractTicketRepository extends JpaRepository<AbstractTicket, Long> {

    @Query("SELECT t " +
            "FROM AbstractTicket t " +
            "WHERE t.validated = true AND t.order.user.id = :userId AND t.order.event.id = :eventId " +
            "ORDER BY t.id " +
            "LIMIT 1")
    Optional<AbstractTicket> findValidatedByUserIdAndEventId(long userId, long eventId);
}
