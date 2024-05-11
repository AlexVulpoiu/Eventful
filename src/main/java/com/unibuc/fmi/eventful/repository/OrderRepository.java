package com.unibuc.fmi.eventful.repository;

import com.unibuc.fmi.eventful.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

}
