package com.unibuc.fmi.eventful.services;

import com.unibuc.fmi.eventful.dto.FeedbackDto;
import com.unibuc.fmi.eventful.dto.request.feedback.AddFeedbackDto;
import com.unibuc.fmi.eventful.exceptions.NotFoundException;
import com.unibuc.fmi.eventful.model.Feedback;
import com.unibuc.fmi.eventful.repository.AbstractUserRepository;
import com.unibuc.fmi.eventful.repository.FeedbackRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FeedbackService {

    AbstractUserRepository abstractUserRepository;
    FeedbackRepository feedbackRepository;

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

    public List<FeedbackDto> getFeedback() {
        var feedbackList = feedbackRepository.findAllOrderedByCreationDateDesc();
        List<FeedbackDto> feedbackDtoList = new ArrayList<>();

        feedbackList.forEach(feedback -> feedbackDtoList.add(
                new FeedbackDto(feedback.getText(), feedback.getAbstractUser().getEmail(), feedback.getDateTime())));

        return feedbackDtoList;
    }
}
