package com.unibuc.fmi.eventful.repository;

import com.unibuc.fmi.eventful.model.Organiser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganiserRepository extends JpaRepository<Organiser, Long> {

}
