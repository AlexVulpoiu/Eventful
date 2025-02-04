package com.unibuc.fmi.eventful.controllers;

import com.unibuc.fmi.eventful.dto.*;
import com.unibuc.fmi.eventful.dto.request.event.AddEventDto;
import com.unibuc.fmi.eventful.dto.request.event.AddPromotionDto;
import com.unibuc.fmi.eventful.dto.request.event.AddRaffleDto;
import com.unibuc.fmi.eventful.dto.request.event.ChangeEventStatusDto;
import com.unibuc.fmi.eventful.enums.EventStatus;
import com.unibuc.fmi.eventful.security.services.UserDetailsImpl;
import com.unibuc.fmi.eventful.services.EventService;
import com.unibuc.fmi.eventful.services.TicketService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventController {

    EventService eventService;
    TicketService ticketService;

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

    @GetMapping
    @PreAuthorize("isAnonymous() || hasAuthority('USER')")
    public Page<EventPreviewDto> getEvents(@RequestParam(required = false) Integer pageNumber,
                                           @RequestParam(required = false) String search) {
        return eventService.getEvents(pageNumber, search);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('ORGANISER', 'MODERATOR', 'ADMIN')")
    public List<EventPreviewDto> getAllEvents(@RequestParam EventStatus status, @RequestParam boolean ended,
                                              @AuthenticationPrincipal UserDetailsImpl principal) {
        return eventService.getAllEvents(status, ended, principal.getId());
    }

    @GetMapping("/{eventId}")
    public EventDto getEventDetails(@PathVariable Long eventId, @AuthenticationPrincipal UserDetailsImpl principal) {
        return eventService.getEventDetails(eventId, principal);
    }

    @GetMapping("/ticketInfo/{eventId}")
    @PreAuthorize("hasAuthority('ORGANISER')")
    public TicketDto getTicketInfo(@PathVariable Long eventId, String ticketId,
                                   @AuthenticationPrincipal UserDetailsImpl principal) {
        return ticketService.getInfo(eventId, ticketId, principal.getId());
    }

    @PostMapping("/validateTicket/{eventId}")
    @PreAuthorize("hasAuthority('ORGANISER')")
    public void validateTicket(@PathVariable Long eventId, @RequestParam String ticketId,
                               @AuthenticationPrincipal UserDetailsImpl principal) {
        ticketService.validate(eventId, ticketId, principal.getId());
    }

    @PostMapping("/{eventId}/promotion")
    @PreAuthorize("hasAuthority('ORGANISER')")
    public EventDto addPromotionForEvent(@PathVariable Long eventId, @Valid @RequestBody AddPromotionDto promotionDto,
                                         @AuthenticationPrincipal UserDetailsImpl principal) {
        return eventService.addPromotion(eventId, promotionDto, principal.getId());
    }

    @PostMapping("/{eventId}/raffle")
    @PreAuthorize("hasAuthority('ORGANISER')")
    public EventDto addRaffleForEvent(@PathVariable Long eventId, @Valid @RequestBody AddRaffleDto raffleDto,
                                      @AuthenticationPrincipal UserDetailsImpl principal) {
        return eventService.addRaffle(eventId, raffleDto, principal.getId());
    }

    @GetMapping("/orders/{eventId}")
    @PreAuthorize("hasAuthority('ORGANISER')")
    public EventOrdersDto getOrdersDetailsForEvent(@PathVariable Long eventId,
                                                   @AuthenticationPrincipal UserDetailsImpl principal) {
        return eventService.getOrdersDetailsForEvent(eventId, principal.getId());
    }

    @GetMapping("/reviews/{eventId}")
    @PreAuthorize("hasAuthority('ORGANISER')")
    public List<ReviewDetailsDto> getReviewsForEvent(@PathVariable Long eventId,
                                                     @AuthenticationPrincipal UserDetailsImpl principal) {
        return eventService.getReviewsForEvent(eventId, principal.getId());
    }

    @PatchMapping("/name/{eventId}")
    @PreAuthorize("hasAuthority('ORGANISER')")
    public void updateEventName(@PathVariable Long eventId, @Valid @RequestBody @NotBlank String name,
                                @AuthenticationPrincipal UserDetailsImpl principal) {
        eventService.updateName(eventId, name, principal.getId());
    }

    @PatchMapping("/description/{eventId}")
    @PreAuthorize("hasAuthority('ORGANISER')")
    public void updateEventDescription(@PathVariable Long eventId, @Valid @RequestBody @NotBlank String description,
                                       @AuthenticationPrincipal UserDetailsImpl principal) {
        eventService.updateDescription(eventId, description, principal.getId());
    }

    @PutMapping("/seatedPrices/{eventId}")
    @PreAuthorize("hasAuthority('ORGANISER')")
    public void updateSeatedPrices(@PathVariable Long eventId, @Valid @RequestBody @NotEmpty Map<Long, Integer> categoriesPrices,
                                   @AuthenticationPrincipal UserDetailsImpl principal) {
        eventService.updateSeatedPrices(eventId, categoriesPrices, principal.getId());
    }

    @PutMapping("/standingPrices/{eventId}")
    @PreAuthorize("hasAuthority('ORGANISER')")
    public void updateStandingPrices(@PathVariable Long eventId, @Valid @RequestBody @NotEmpty Map<String, Integer> categoriesPrices,
                                     @AuthenticationPrincipal UserDetailsImpl principal) {
        eventService.updateStandingPrices(eventId, categoriesPrices, principal.getId());
    }
}
