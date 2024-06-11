package com.unibuc.fmi.eventful.repository;

import com.unibuc.fmi.eventful.model.AbstractLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AbstractLocationRepository extends JpaRepository<AbstractLocation, Long> {

    @Query("SELECT l " +
            "FROM AbstractLocation l " +
            "WHERE l.name LIKE %:search%")
    List<AbstractLocation> findAllByName(String search);
}
