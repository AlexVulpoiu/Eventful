package com.unibuc.fmi.eventful.repository;

import com.unibuc.fmi.eventful.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o " +
            "FROM Order o " +
            "WHERE o.paymentSession.id = :sessionId")
    Optional<Order> findByPaymentSessionId(String sessionId);
}
