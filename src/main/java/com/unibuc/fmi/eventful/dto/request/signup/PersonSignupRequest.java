package com.unibuc.fmi.eventful.dto.request.signup;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PersonSignupRequest extends OrganiserSignupRequest {

    private String cnp;
}
