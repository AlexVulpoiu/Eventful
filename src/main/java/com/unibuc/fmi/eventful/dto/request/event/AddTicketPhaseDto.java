package com.unibuc.fmi.eventful.dto.request.event;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddTicketPhaseDto {

    @NotBlank
    private String name;

    private int price;

    @Future
    private LocalDate dateLimit;
}
