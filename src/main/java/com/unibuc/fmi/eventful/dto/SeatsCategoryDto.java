package com.unibuc.fmi.eventful.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatsCategoryDto {

    private Long id;

    private String name;

    private int minRow;

    private int maxRow;

    private int minSeat;

    private int maxSeat;

    private String color;
}
