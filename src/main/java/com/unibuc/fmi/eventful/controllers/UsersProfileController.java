package com.unibuc.fmi.eventful.controllers;

import com.unibuc.fmi.eventful.dto.ProfileDto;
import com.unibuc.fmi.eventful.security.services.UserDetailsImpl;
import com.unibuc.fmi.eventful.services.ProfileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/profile")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UsersProfileController {

    ProfileService profileService;

    @GetMapping
    @PreAuthorize("hasAuthority('USER')")
    public ProfileDto getProfileDetails(@AuthenticationPrincipal UserDetailsImpl principal) {
        return profileService.getProfileDetails(principal.getId());
    }
}
