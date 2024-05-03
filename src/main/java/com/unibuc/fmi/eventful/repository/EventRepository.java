package com.unibuc.fmi.eventful.repository;

import com.unibuc.fmi.eventful.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

}
