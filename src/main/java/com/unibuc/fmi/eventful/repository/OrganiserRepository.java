package com.unibuc.fmi.eventful.repository;

import com.unibuc.fmi.eventful.enums.OrganiserStatus;
import com.unibuc.fmi.eventful.model.Organiser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganiserRepository extends JpaRepository<Organiser, Long> {

    @Query("SELECT o " +
            "FROM Organiser o " +
            "WHERE o.status = :status " +
            "ORDER BY (o.firstName || ' ' || o.lastName) ASC")
    List<Organiser> findByStatus(OrganiserStatus status);
}
