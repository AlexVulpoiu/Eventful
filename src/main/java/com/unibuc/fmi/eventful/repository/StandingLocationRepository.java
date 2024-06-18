package com.unibuc.fmi.eventful.repository;

import com.unibuc.fmi.eventful.model.StandingLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StandingLocationRepository extends JpaRepository<StandingLocation, Long> {

    @Query("SELECT sl " +
            "FROM StandingLocation sl " +
            "WHERE sl.name LIKE %:search% OR sl.city LIKE %:search% " +
            "ORDER BY sl.name ASC")
    List<StandingLocation> getStandingLocationsOrderedByName(String search);
}
