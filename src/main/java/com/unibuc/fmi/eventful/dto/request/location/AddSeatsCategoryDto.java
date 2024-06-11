package com.unibuc.fmi.eventful.dto.request.location;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddSeatsCategoryDto {

    @NotBlank
    private String name;

    @Min(value = 1)
    private int minRow;

    @Min(value = 1)
    private int maxRow;

    @Min(value = 1)
    private int minSeat;

    @Min(value = 1)
    private int maxSeat;
}
