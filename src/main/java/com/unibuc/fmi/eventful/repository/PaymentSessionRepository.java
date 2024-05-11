package com.unibuc.fmi.eventful.repository;

import com.unibuc.fmi.eventful.model.PaymentSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentSessionRepository extends JpaRepository<PaymentSession, String> {

}
