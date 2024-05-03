package com.unibuc.fmi.eventful.dto.request.event;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddStandingCategoryDto {

    @NotBlank
    private String name;

    @Min(0)
    private int capacity;

    @NotEmpty
    private List<AddTicketPhaseDto> ticketPhases;
}
