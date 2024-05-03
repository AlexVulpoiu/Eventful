package com.unibuc.fmi.eventful.repository;

import com.unibuc.fmi.eventful.model.TicketPhase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketPhaseRepository extends JpaRepository<TicketPhase, Long> {

}
