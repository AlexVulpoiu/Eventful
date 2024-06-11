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
public class TicketDto {

    private String externalId;

    private long eventId;

    private String eventName;

    private LocalDateTime startDate;

    private String locationAddress;

    private boolean validated;

    private String category;

    private int row = 0;

    private int seat = 0;
}
