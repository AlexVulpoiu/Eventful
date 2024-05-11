package com.unibuc.fmi.eventful.repository;

import com.unibuc.fmi.eventful.model.StandingTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StandingTicketRepository extends JpaRepository<StandingTicket, Long> {

}
