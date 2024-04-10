package com.unibuc.fmi.eventful.dto.request.signup;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public abstract class BaseSignupRequest {

    @Email
    protected String email;

    @NotBlank
    protected String firstName;

    @NotBlank
    protected String lastName;

    @NotBlank
    protected String password;
}
