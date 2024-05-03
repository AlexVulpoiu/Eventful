package com.unibuc.fmi.eventful.repository;

import com.unibuc.fmi.eventful.model.CharitableCause;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CharitableCauseRepository extends JpaRepository<CharitableCause, Long> {

}
