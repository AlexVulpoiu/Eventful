package com.unibuc.fmi.eventful.dto.request.location;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public abstract class AbstractLocationDto {

    protected Long id;

    protected String name;

    protected String city;

    protected String country;

    protected String address;
}
