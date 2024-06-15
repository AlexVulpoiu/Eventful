package com.unibuc.fmi.eventful.controllers;

import com.unibuc.fmi.eventful.dto.request.event.AddReviewDto;
import com.unibuc.fmi.eventful.security.services.UserDetailsImpl;
import com.unibuc.fmi.eventful.services.ReviewService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewController {

    ReviewService reviewService;

    @GetMapping("/{reviewId}")
    @PreAuthorize("hasAuthority('USER')")
    public void checkReviewAccess(@PathVariable UUID reviewId, @AuthenticationPrincipal UserDetailsImpl principal) {
        reviewService.checkAccess(reviewId, principal.getId());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER')")
    public void addReview(@Valid @RequestBody AddReviewDto addReviewDto,
                          @AuthenticationPrincipal UserDetailsImpl principal) {
        reviewService.addReview(addReviewDto, principal.getId());
    }
}
