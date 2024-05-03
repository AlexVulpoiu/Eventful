package com.unibuc.fmi.eventful.repository;

import com.unibuc.fmi.eventful.model.SeatsCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatsCategoryRepository extends JpaRepository<SeatsCategory, Long> {

}
