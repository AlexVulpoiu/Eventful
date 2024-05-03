package com.unibuc.fmi.eventful.dto.request.location;

import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AddStandingLocationDto extends AddAbstractLocationDto {

    @Min(value = 1)
    private int capacity;
}
