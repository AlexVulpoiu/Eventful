package com.unibuc.fmi.eventful.repository;

import com.unibuc.fmi.eventful.model.PaymentIntent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, String> {

}
