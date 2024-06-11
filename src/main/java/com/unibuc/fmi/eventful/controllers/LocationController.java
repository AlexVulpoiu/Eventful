package com.unibuc.fmi.eventful.controllers;

import com.unibuc.fmi.eventful.dto.LocationDetailsDto;
import com.unibuc.fmi.eventful.dto.SeatedLocationDto;
import com.unibuc.fmi.eventful.dto.StandingLocationDto;
import com.unibuc.fmi.eventful.dto.request.location.AddSeatedLocationDto;
import com.unibuc.fmi.eventful.dto.request.location.AddStandingLocationDto;
import com.unibuc.fmi.eventful.services.LocationService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/locations")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocationController {

    LocationService locationService;

    @PostMapping("/standing")
    @PreAuthorize("hasAnyAuthority('MODERATOR', 'ADMIN')")
    public Long addStandingLocation(@Valid @RequestBody AddStandingLocationDto addStandingLocationDto) {
        return locationService.addStandingLocation(addStandingLocationDto);
    }

    @PostMapping("/seated")
    @PreAuthorize("hasAnyAuthority('MODERATOR', 'ADMIN')")
    public Long addSeatedLocation(@Valid @RequestBody AddSeatedLocationDto addSeatedLocationDto) {
        return locationService.addSeatedLocation(addSeatedLocationDto);
    }

    @GetMapping("/standing")
    @PreAuthorize("isAuthenticated()")
    public List<StandingLocationDto> getStandingLocations(@RequestParam(required = false) String search) {
        return locationService.getStandingLocations(search);
    }

    @GetMapping("/seated")
    @PreAuthorize("isAuthenticated()")
    public List<SeatedLocationDto> getSeatedLocations(@RequestParam(required = false) String search) {
        return locationService.getSeatedLocations(search);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ORGANISER')")
    public List<LocationDetailsDto> getLocations(@RequestParam(required = false) String search) {
        return locationService.getLocations(search);
    }
}
