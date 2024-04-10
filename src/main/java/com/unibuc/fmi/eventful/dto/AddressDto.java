package com.unibuc.fmi.eventful.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {

    @NotBlank
    private String country;

    @NotBlank
    private String district;

    @NotBlank
    private String city;

    @NotBlank
    private String street;

    @NotBlank
    private String building;

    @NotBlank
    private String zipCode;
}
