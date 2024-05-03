package com.unibuc.fmi.eventful.dto.request.location;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AddSeatedLocationDto extends AddAbstractLocationDto {

    @Min(value = 1)
    private int numberOfRows;

    @Min(value = 1)
    private int seatsPerRow;

    @NotEmpty
    private List<@Valid AddSeatsCategoryDto> seatsCategories;
}
