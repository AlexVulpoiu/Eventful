package com.unibuc.fmi.eventful.dto.request.location;

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

    private int minRow;

    private int maxRow;

    private int minSeat;

    private int maxSeat;

    @NotBlank
    private String color;
}
