package com.unibuc.fmi.eventful.repository;

import com.unibuc.fmi.eventful.model.LegalPerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LegalPersonRepository extends JpaRepository<LegalPerson, Long> {

}
