package com.unibuc.fmi.eventful.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CharitableCauseDto {

    private Long id;

    private String name;

    private String description;

    private int neededAmount;

    private double collectedAmount;

    private LocalDateTime endDate;
}
