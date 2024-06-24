package com.unibuc.fmi.eventful.services;

import com.unibuc.fmi.eventful.dto.request.event.AddReviewDto;
import com.unibuc.fmi.eventful.exceptions.BadRequestException;
import com.unibuc.fmi.eventful.exceptions.ForbiddenException;
import com.unibuc.fmi.eventful.exceptions.NotFoundException;
import com.unibuc.fmi.eventful.model.Review;
import com.unibuc.fmi.eventful.repository.EventRepository;
import com.unibuc.fmi.eventful.repository.OrderRepository;
import com.unibuc.fmi.eventful.repository.ReviewRepository;
import com.unibuc.fmi.eventful.repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewService {

    EventRepository eventRepository;
    OrderRepository orderRepository;
    ReviewRepository reviewRepository;
    UserRepository userRepository;
    SendEmailService sendEmailService;

    @Scheduled(cron = "0 0 9 ? * *")
    public void notifyUsersForEventsReviews() throws MessagingException, UnsupportedEncodingException {
        log.info("Starting job for sending events reviews notifications");
        var events = eventRepository.getEventsEndedAt(LocalDate.now().minusDays(2));

        for (var event : events) {
            log.info("Sending notifications for event {}", event.getId());
            var participants = orderRepository.getParticipantsForEvent(event.getId());
            for (var participant : participants) {
                var user = userRepository.findById(participant)
                        .orElseThrow(() -> new NotFoundException("User with id " + participant + " doesn't exist!"));

                log.info("Sending notification to user {}", user.getId());
                Review review = reviewRepository.save(new Review(UUID.randomUUID(), null, null, event, user));
                sendEmailService.sendReviewReminder(review);
            }
        }

        log.info("Ending job for sending events reviews notifications");
    }

    public void checkAccess(UUID reviewId, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found!"));

        var review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review with id " + reviewId + " doesn't exist!"));
        if (!review.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You are not allowed to perform this operation!");
        }
        if (review.getText() != null) {
            throw new BadRequestException("The review can't be completed more than once!");
        }
    }

    @Transactional
    public void addReview(AddReviewDto addReviewDto, long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found!"));

        var review = reviewRepository.findById(addReviewDto.getReviewId())
                .orElseThrow(() -> new NotFoundException("Review with id " + addReviewDto.getReviewId() + " doesn't exist!"));
        if (!review.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You are not allowed to perform this operation!");
        }
        if (review.getText() != null) {
            throw new BadRequestException("The review can't be completed more than once!");
        }

        review.setText(addReviewDto.getText());
        review.setDateTime(LocalDateTime.now());
        reviewRepository.save(review);

        user.addPoints(10);
        userRepository.save(user);
    }
}
