package com.unibuc.fmi.eventful.repository;

import com.unibuc.fmi.eventful.model.AbstractTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AbstractTicketRepository extends JpaRepository<AbstractTicket, Long> {

}
