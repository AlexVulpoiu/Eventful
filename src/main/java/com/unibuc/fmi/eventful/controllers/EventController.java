package com.unibuc.fmi.eventful.controllers;

import com.unibuc.fmi.eventful.dto.request.event.AddEventDto;
import com.unibuc.fmi.eventful.dto.request.event.ChangeEventStatusDto;
import com.unibuc.fmi.eventful.enums.EventStatus;
import com.unibuc.fmi.eventful.security.services.UserDetailsImpl;
import com.unibuc.fmi.eventful.services.EventService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventController {

    EventService eventService;

    @PostMapping
    @PreAuthorize("hasAuthority('ORGANISER')")
    public long addEvent(@Valid @RequestBody AddEventDto addEventDto,
                         @AuthenticationPrincipal UserDetailsImpl principal) {
        var event = eventService.addEvent(addEventDto, principal.getId());
        return event.getId();
    }

    @PatchMapping("/{eventId}")
    @PreAuthorize("hasAuthority('ORGANISER')")
    public void updateEventLogo(@PathVariable long eventId, @RequestParam MultipartFile logo,
                                @AuthenticationPrincipal UserDetailsImpl principal) throws IOException {
        eventService.updateLogo(eventId, logo, principal.getId());
    }

    @PatchMapping("/status/{eventId}")
    @PreAuthorize("hasAnyAuthority('MODERATOR', 'ADMIN')")
    public EventStatus updateEventStatus(@PathVariable long eventId,
                                         @Valid @RequestBody ChangeEventStatusDto changeEventStatusDto)
            throws MessagingException, UnsupportedEncodingException {
        var event = eventService.updateStatus(eventId, changeEventStatusDto);
        return event.getStatus();
    }
}
