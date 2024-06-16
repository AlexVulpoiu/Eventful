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
public class EventOrdersDto {

    private double totalAmount;

    private boolean charitableEvent;

    private double charityAmount;

    private List<OrderForEventDetailsDto> orders;
}
