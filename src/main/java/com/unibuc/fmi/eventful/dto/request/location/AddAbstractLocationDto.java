package com.unibuc.fmi.eventful.dto.request.location;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public abstract class AddAbstractLocationDto {

    @NotBlank
    protected String name;

    @NotBlank
    protected String city;

    @NotBlank
    protected String country;

    @NotBlank
    protected String address;
}
