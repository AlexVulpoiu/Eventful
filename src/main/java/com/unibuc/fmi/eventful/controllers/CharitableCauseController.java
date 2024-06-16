package com.unibuc.fmi.eventful.controllers;

import com.unibuc.fmi.eventful.dto.request.charitablecause.AddOrEditCharitableCauseDto;
import com.unibuc.fmi.eventful.security.services.UserDetailsImpl;
import com.unibuc.fmi.eventful.services.CharitableCauseService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/charitableCauses")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CharitableCauseController {

    CharitableCauseService charitableCauseService;

    @PutMapping("/{causeId}")
    @PreAuthorize("hasAuthority('ORGANISER')")
    public long editCharitableCause(@PathVariable Long causeId, @Valid @RequestBody AddOrEditCharitableCauseDto editCharitableCauseDto,
                                    @AuthenticationPrincipal UserDetailsImpl principal) {
        var charitableCause = charitableCauseService.editCharitableCause(causeId, editCharitableCauseDto, principal.getId());
        return charitableCause.getId();
    }
}
