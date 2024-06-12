package com.unibuc.fmi.eventful.repository;

import com.unibuc.fmi.eventful.model.Order;
import com.unibuc.fmi.eventful.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o " +
            "FROM Order o " +
            "WHERE o.paymentSession.id = :sessionId")
    Optional<Order> findByPaymentSessionId(String sessionId);

    @Query("SELECT o.user " +
            "FROM Order o " +
            "WHERE o.event.id = :eventId AND o.orderDate <= :date")
    List<User> getCustomersForEventUntil(long eventId, LocalDate date);
}
