package com.unibuc.fmi.eventful.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganiserStatisticsDto {

    private List<String> months;

    private List<Double> incomePerMonth;

    private List<Integer> eventsPerMonth;

    private double charityAmount;

    private int causesThisYear;

    private int causesLastYear;

    private double charityIncrease;
}
