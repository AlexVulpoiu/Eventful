package com.unibuc.fmi.eventful.repository;

import com.unibuc.fmi.eventful.enums.EventStatus;
import com.unibuc.fmi.eventful.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e " +
            "FROM Event e " +
            "WHERE e.status = 'ACCEPTED' AND lower(e.name) LIKE %:search% AND e.endDate >= :dateTime " +
            "ORDER BY e.startDate ASC")
    Page<Event> searchEventsByNameInChronologicalOrderEndingAfter(String search, LocalDateTime dateTime, Pageable pageable);

    @Query("SELECT e " +
            "FROM Event e " +
            "WHERE e.status = 'REJECTED' AND e.updatedAt < :dateTime")
    List<Event> getRejectedEventsNotUpdatedSince(LocalDateTime dateTime);

    @Query("SELECT e " +
            "FROM Event e " +
            "WHERE DATE(e.endDate) = :date")
    List<Event> getEventsEndedAt(LocalDate date);

    @Query("SELECT e " +
            "FROM Event e " +
            "WHERE e.status = :status AND DATE(e.endDate) >= :date " +
            "ORDER BY e.startDate ASC")
    List<Event> getEventsByStatusEndingAfter(EventStatus status, LocalDate date);

    @Query("SELECT e " +
            "FROM Event e " +
            "WHERE e.organiser.id = :organiserId AND e.status = :status AND DATE(e.endDate) >= :date " +
            "ORDER BY e.startDate ASC")
    List<Event> getEventsForOrganiserByStatusEndingAfter(Long organiserId, EventStatus status, LocalDate date);

    @Query("SELECT e " +
            "FROM Event e " +
            "WHERE e.status = 'ACCEPTED' AND DATE(e.endDate) < :date")
    List<Event> getEventsEndedBefore(LocalDate date);

    @Query("SELECT e " +
            "FROM Event e " +
            "WHERE e.organiser.id = :organiserId AND e.status = 'ACCEPTED' AND DATE(e.endDate) < :date")
    List<Event> getEventsForOrganiserEndedBefore(Long organiserId, LocalDate date);

    @Query("SELECT e " +
            "FROM Event e " +
            "WHERE e.organiser.id = :organiserId AND e.status = 'ACCEPTED' " +
            "   AND MONTH(e.startDate) = :month AND YEAR(e.startDate) = :year")
    List<Event> getEventsForOrganiserHavingStartingMonth(Long organiserId, int month, int year);

    @Query("SELECT e " +
            "FROM Event e " +
            "WHERE e.organiser.id = :organiserId AND e.status = 'ACCEPTED' AND e.charityPercentage > 0 AND YEAR(e.endDate) = :year")
    List<Event> getCharitableEventsByOrganiserAndYear(Long organiserId, int year);

    @Query("SELECT e " +
            "FROM Event e " +
            "WHERE e.status = 'ACCEPTED' AND MONTH(e.startDate) = :month AND YEAR(e.startDate) = :year")
    List<Event> getEventsByStartMonth(int month, int year);
}
