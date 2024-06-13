package com.unibuc.fmi.eventful.services;

import com.unibuc.fmi.eventful.dto.request.feedback.AddFeedbackDto;
import com.unibuc.fmi.eventful.exceptions.NotFoundException;
import com.unibuc.fmi.eventful.model.Feedback;
import com.unibuc.fmi.eventful.repository.AbstractUserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FeedbackService {

    AbstractUserRepository abstractUserRepository;

    public void addFeedback(AddFeedbackDto feedbackDto, Long userId) {
        var user = abstractUserRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found!"));
        Feedback feedback = Feedback.builder()
                .text(feedbackDto.getText())
                .dateTime(LocalDateTime.now())
                .abstractUser(user)
                .build();
        user.getFeedbackList().add(feedback);
        abstractUserRepository.save(user);
    }
}
