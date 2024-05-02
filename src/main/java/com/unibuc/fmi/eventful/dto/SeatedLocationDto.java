package com.unibuc.fmi.eventful.dto;

import com.unibuc.fmi.eventful.dto.request.location.AbstractLocationDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SeatedLocationDto extends AbstractLocationDto {

    private int numberOfRows;

    private int seatsPerRow;

    private List<SeatsCategoryDto> seatsCategories;
}
