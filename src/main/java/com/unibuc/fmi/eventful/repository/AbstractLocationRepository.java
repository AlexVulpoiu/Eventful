package com.unibuc.fmi.eventful.repository;

import com.unibuc.fmi.eventful.model.AbstractLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AbstractLocationRepository extends JpaRepository<AbstractLocation, Long> {

}
