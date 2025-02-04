package com.unibuc.fmi.eventful.dto.request.event;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class AddRaffleDto {

    @Min(0)
    private int participantsLimit;

    private LocalDate endDate;

    @Min(1)
    @Max(100)
    private int prize;

    @NotBlank
    private String partnerName;
}
