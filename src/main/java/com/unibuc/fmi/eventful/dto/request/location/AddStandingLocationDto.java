package com.unibuc.fmi.eventful.dto.request.location;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AddStandingLocationDto extends AddAbstractLocationDto {

    private int capacity;
}
