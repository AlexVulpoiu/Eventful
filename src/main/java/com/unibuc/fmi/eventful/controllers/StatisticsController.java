package com.unibuc.fmi.eventful.controllers;

import com.unibuc.fmi.eventful.dto.OrganiserStatisticsDto;
import com.unibuc.fmi.eventful.security.services.UserDetailsImpl;
import com.unibuc.fmi.eventful.services.StatisticsService;
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
@RequestMapping("/statistics")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StatisticsController {

    StatisticsService statisticsService;

    @GetMapping
    @PreAuthorize("hasAuthority('ORGANISER')")
    public OrganiserStatisticsDto getStatistics(@AuthenticationPrincipal UserDetailsImpl principal) {
        return statisticsService.getStatistics(principal.getId());
    }
}
