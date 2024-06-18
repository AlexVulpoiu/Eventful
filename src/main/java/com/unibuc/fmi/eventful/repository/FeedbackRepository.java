package com.unibuc.fmi.eventful.repository;

import com.unibuc.fmi.eventful.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    @Query("SELECT f " +
            "FROM Feedback f " +
            "ORDER BY f.dateTime DESC")
    List<Feedback> findAllOrderedByCreationDateDesc();
}
