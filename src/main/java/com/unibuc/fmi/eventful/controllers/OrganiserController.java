package com.unibuc.fmi.eventful.controllers;

import com.unibuc.fmi.eventful.dto.OrganiserProfileDto;
import com.unibuc.fmi.eventful.enums.OrganiserStatus;
import com.unibuc.fmi.eventful.services.OrganiserService;
import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/organisers")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrganiserController {

    OrganiserService organiserService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('MODERATOR', 'ADMIN')")
    public List<OrganiserProfileDto> getOrganisers(@RequestParam OrganiserStatus status) {
        return organiserService.getOrganisersByStatus(status);
    }

    @PatchMapping("/{organiserId}")
    @PreAuthorize("hasAnyAuthority('MODERATOR', 'ADMIN')")
    public void updateOrganiserStatus(@PathVariable Long organiserId, @RequestParam OrganiserStatus status) throws MessagingException, UnsupportedEncodingException {
        organiserService.updateOrganiserStatus(organiserId, status);
    }
}
