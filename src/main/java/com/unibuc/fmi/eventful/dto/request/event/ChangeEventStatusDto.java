package com.unibuc.fmi.eventful.dto.request.event;

import com.unibuc.fmi.eventful.enums.EventStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeEventStatusDto {

    @NotNull
    private EventStatus status;

    private String reason;
}
