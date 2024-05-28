package com.unibuc.fmi.eventful.repository;

import com.unibuc.fmi.eventful.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT count(r) " +
            "FROM Review r " +
            "WHERE r.event.id = :eventId AND r.user.id = :userId")
    int getNumberOfReviewsPerEventByUser(long eventId, long userId);

    @Query("SELECT r " +
            "FROM Review r " +
            "WHERE r.event.id = :eventId AND r.user.id = :userId " +
            "ORDER BY r.dateTime DESC " +
            "LIMIT 1")
    Optional<Review> getLastReviewFromEventByUser(long eventId, long userId);
}
