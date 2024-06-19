package com.unibuc.fmi.eventful.repository;

import com.unibuc.fmi.eventful.model.Order;
import com.unibuc.fmi.eventful.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o " +
            "FROM Order o " +
            "WHERE o.paymentSession.id = :sessionId")
    Optional<Order> findByPaymentSessionId(String sessionId);

    @Query("SELECT DISTINCT o.user " +
            "FROM Order o " +
            "WHERE o.event.id = :eventId AND o.orderDate <= :date")
    List<User> getCustomersForEventUntil(long eventId, LocalDateTime date);

    @Query("SELECT DISTINCT o.user.id " +
            "FROM Order o " +
            "WHERE o.event.id = :eventId AND EXISTS (SELECT t " +
"                                                    FROM AbstractTicket t " +
        "                                            WHERE t.order.id = o.id AND t.validated = true)")
    List<Long> getParticipantsForEvent(long eventId);

    @Query("SELECT o " +
            "FROM Order o " +
            "WHERE o.paymentSession.paymentStatus = 'COMPLETE' AND o.event.id = :eventId " +
            "ORDER BY o.orderDate DESC")
    List<Order> getOrdersForEvent(Long eventId);

    @Query("SELECT SUM(o.total) " +
            "FROM Order o " +
            "WHERE o.paymentSession IS NOT null AND o.paymentSession.paymentStatus = 'COMPLETE' AND o.event.id = :eventId")
    Double getIncomeForEvent(Long eventId);
}
