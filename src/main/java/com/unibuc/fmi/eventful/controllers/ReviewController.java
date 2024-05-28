package com.unibuc.fmi.eventful.controllers;

import com.unibuc.fmi.eventful.dto.ReviewDto;
import com.unibuc.fmi.eventful.dto.request.event.AddReviewDto;
import com.unibuc.fmi.eventful.security.services.UserDetailsImpl;
import com.unibuc.fmi.eventful.services.ReviewService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewController {

    ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasAuthority('USER')")
    public ReviewDto addReview(@Valid @RequestBody AddReviewDto addReviewDto,
                               @AuthenticationPrincipal UserDetailsImpl principal) {
        return reviewService.addReview(addReviewDto, principal.getId());
    }
}
