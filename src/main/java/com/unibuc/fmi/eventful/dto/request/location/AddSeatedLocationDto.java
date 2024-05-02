package com.unibuc.fmi.eventful.dto.request.location;

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

    private int numberOfRows;

    private int seatsPerRow;

    @NotEmpty
    private List<AddSeatsCategoryDto> seatsCategories;
}
