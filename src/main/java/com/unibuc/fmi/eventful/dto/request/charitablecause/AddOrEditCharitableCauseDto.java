package com.unibuc.fmi.eventful.dto.request.charitablecause;

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
public class AddOrEditCharitableCauseDto {

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @Min(value = 1)
    private int neededAmount;
}
