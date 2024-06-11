package com.unibuc.fmi.eventful.repository;

import com.unibuc.fmi.eventful.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e " +
            "FROM Event e " +
            "WHERE lower(e.name) LIKE %:search% AND e.endDate >= :dateTime " +
            "ORDER BY e.startDate ASC")
    List<Event> searchEventsByNameInChronologicalOrderEndingAfter(String search, LocalDateTime dateTime);
}
