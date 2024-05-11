package com.unibuc.fmi.eventful.repository;

import com.unibuc.fmi.eventful.model.SeatedTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeatedTicketRepository extends JpaRepository<SeatedTicket, Long> {

    @Query("SELECT st" +
            " FROM SeatedTicket st" +
            " WHERE st.numberOfRow = :row AND st.seat = :seat AND st.categoryPrice.category.id = :categoryId" +
            "    AND st.categoryPrice.event.id = :eventId")
    Optional<SeatedTicket> findByRowAndSeatAndCategoryAndEvent(int row, int seat, long categoryId, long eventId);
}
