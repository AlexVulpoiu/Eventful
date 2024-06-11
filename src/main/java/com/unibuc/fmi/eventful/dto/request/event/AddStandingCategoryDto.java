package com.unibuc.fmi.eventful.dto.request.event;

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
public class AddStandingCategoryDto {

    @NotBlank
    private String name;

    @Min(0)
    private int capacity;

    @Min(0)
    private int price;
}
