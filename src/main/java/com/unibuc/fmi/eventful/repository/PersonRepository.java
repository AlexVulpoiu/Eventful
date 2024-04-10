package com.unibuc.fmi.eventful.repository;

import com.unibuc.fmi.eventful.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

}
