package com.unibuc.fmi.eventful.dto.request.signup;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LegalPersonSignupRequest extends OrganiserSignupRequest {

    private long cui;

    @NotBlank
    private String name;
}
