package com.unibuc.fmi.eventful.controllers;

import com.unibuc.fmi.eventful.dto.FeedbackDto;
import com.unibuc.fmi.eventful.dto.request.feedback.AddFeedbackDto;
import com.unibuc.fmi.eventful.security.services.UserDetailsImpl;
import com.unibuc.fmi.eventful.services.FeedbackService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/feedback")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FeedbackController {

    FeedbackService feedbackService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public void addFeedback(@Valid @RequestBody AddFeedbackDto feedbackDto, @AuthenticationPrincipal UserDetailsImpl principal) {
        feedbackService.addFeedback(feedbackDto, principal.getId());
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('MODERATOR', 'ADMIN')")
    public List<FeedbackDto> getFeedback() {
        return feedbackService.getFeedback();
    }
}
