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
public class EventPreviewDto {

    private Long id;

    private String name;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private String location;

    private String logo;
}
