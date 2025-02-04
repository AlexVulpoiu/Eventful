package com.unibuc.fmi.eventful.repository;

import com.unibuc.fmi.eventful.model.AbstractTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AbstractTicketRepository extends JpaRepository<AbstractTicket, Long> {

    @Query("SELECT t " +
            "FROM AbstractTicket t " +
            "WHERE t.externalId = :externalId AND t.order.event.id = :eventId " +
            "ORDER BY t.id " +
            "LIMIT 1")
    Optional<AbstractTicket> findByExternalIdAndEventId(String externalId, long eventId);

    @Query("SELECT t " +
            "FROM AbstractTicket t " +
            "WHERE t.order.event.id = :eventId")
    List<AbstractTicket> findTicketsForEvent(Long eventId);
}
