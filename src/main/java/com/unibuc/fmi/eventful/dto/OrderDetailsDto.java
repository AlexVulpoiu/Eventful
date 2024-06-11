package com.unibuc.fmi.eventful.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailsDto {

    private long id;

    private String status;

    private String eventName;

    private LocalDateTime orderDate;

    private double total;

    private List<TicketDetailsDto> tickets;
}
