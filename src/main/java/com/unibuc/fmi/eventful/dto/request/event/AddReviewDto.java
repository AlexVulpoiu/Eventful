package com.unibuc.fmi.eventful.dto.request.event;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddReviewDto {

    private long eventId;

    @NotBlank
    private String text;
}
