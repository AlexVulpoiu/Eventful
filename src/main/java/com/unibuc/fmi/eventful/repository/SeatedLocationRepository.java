package com.unibuc.fmi.eventful.repository;

import com.unibuc.fmi.eventful.model.SeatedLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatedLocationRepository extends JpaRepository<SeatedLocation, Long> {

    @Query("SELECT sl " +
            "FROM SeatedLocation sl " +
            "WHERE sl.name LIKE %:search% OR sl.city LIKE %:search% " +
            "ORDER BY sl.name ASC")
    List<SeatedLocation> getSeatedLocationsOrderedByName(String search);
}
