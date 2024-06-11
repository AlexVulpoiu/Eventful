package com.unibuc.fmi.eventful.services;

import com.unibuc.fmi.eventful.dto.ReviewDto;
import com.unibuc.fmi.eventful.dto.request.event.AddReviewDto;
import com.unibuc.fmi.eventful.exceptions.BadRequestException;
import com.unibuc.fmi.eventful.exceptions.NotFoundException;
import com.unibuc.fmi.eventful.mappers.ReviewMapper;
import com.unibuc.fmi.eventful.model.Review;
import com.unibuc.fmi.eventful.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewService {

    AbstractTicketRepository abstractTicketRepository;
    EventRepository eventRepository;
    OrganiserRepository organiserRepository;
    ReviewRepository reviewRepository;
    UserRepository userRepository;
    ReviewMapper reviewMapper;

    // TODO: refactor logic
    @Transactional
    public ReviewDto addReview(AddReviewDto addReviewDto, long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found!"));

        long eventId = addReviewDto.getEventId();
        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " not found!"));
        if (LocalDateTime.now().isBefore(event.getEndDate())) {
            throw new BadRequestException("Reviews can be added only after the end of the event!");
        }

        if (reviewRepository.getNumberOfReviewsPerEventByUser(eventId, userId) >= 2) {
            throw new BadRequestException("You can't add more than two reviews per event!");
        }
        var lastReview = reviewRepository.getLastReviewFromEventByUser(eventId, userId);
        if (lastReview.isPresent() && LocalDateTime.now().minusHours(72).isBefore(lastReview.get().getDateTime())) {
            throw new BadRequestException("You have to wait for at least 3 days to add one more review for the same event!");
        }

        var isParticipant = abstractTicketRepository.findValidatedByUserIdAndEventId(userId, eventId).isPresent();
        var review = Review.builder()
                .text(addReviewDto.getText())
                .rating(addReviewDto.getRating())
                .participant(isParticipant)
                .dateTime(LocalDateTime.now())
                .event(event)
                .user(user)
                .build();

        if (isParticipant) {
            user.addPoints(10);
            userRepository.save(user);
        }

        event.addReview(review);
        eventRepository.save(event);

        var organiser = event.getOrganiser();
        organiser.updateRating();
        organiserRepository.save(organiser);

        return reviewMapper.reviewToReviewDto(review);
    }
}
