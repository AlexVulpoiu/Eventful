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
public class GeneralStatisticsDto {

    private List<String> months;

    private List<Double> incomePerMonth;

    private List<Integer> eventsPerMonth;

    private double totalIncome;

    private int totalEvents;

    private int totalTicketsSold;

    private int totalUsers;
}
