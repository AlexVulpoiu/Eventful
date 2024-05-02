package com.unibuc.fmi.eventful.dto;

import com.unibuc.fmi.eventful.dto.request.location.AbstractLocationDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StandingLocationDto extends AbstractLocationDto {

    private int capacity;
}
