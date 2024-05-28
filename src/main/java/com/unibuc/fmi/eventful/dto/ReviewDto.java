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
public class ReviewDto {

    private long id;

    private String text;

    private int rating;

    private boolean participant;

    private LocalDateTime dateTime;

    private long userId;
}
