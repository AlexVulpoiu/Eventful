package com.unibuc.fmi.eventful.dto.request.event;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddPromotionDto {

    @Min(1)
    @Max(100)
    private int value;

    @Future
    private LocalDate endDate;
}
