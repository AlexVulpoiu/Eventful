package com.unibuc.fmi.eventful.controllers;

import com.unibuc.fmi.eventful.dto.CharitableCauseDto;
import com.unibuc.fmi.eventful.dto.request.charitablecause.AddCharitableCauseDto;
import com.unibuc.fmi.eventful.security.services.UserDetailsImpl;
import com.unibuc.fmi.eventful.services.CharitableCauseService;
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
@RequestMapping("/charitableCauses")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CharitableCauseController {

    CharitableCauseService charitableCauseService;

    @PostMapping
    @PreAuthorize("hasAuthority('ORGANISER')")
    public long addCharitableCause(@Valid @RequestBody AddCharitableCauseDto addCharitableCauseDto,
                                   @AuthenticationPrincipal UserDetailsImpl principal) {
        var charitableCause = charitableCauseService.addCharitableCause(addCharitableCauseDto, principal.getId());
        return charitableCause.getId();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ORGANISER')")
    public List<CharitableCauseDto> getCharitableCausesForOrganiser(@RequestParam(required = false) String search,
                                                                    @AuthenticationPrincipal UserDetailsImpl principal) {
        return charitableCauseService.getCharitableCausesForOrganiser(principal.getId(), search);
    }
}
