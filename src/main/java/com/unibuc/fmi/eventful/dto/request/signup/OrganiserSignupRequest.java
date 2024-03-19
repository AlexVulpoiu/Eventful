package com.unibuc.fmi.eventful.dto.request.signup;


import com.unibuc.fmi.eventful.dto.AddressDto;
import com.unibuc.fmi.eventful.dto.BankAccountDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class OrganiserSignupRequest extends BaseSignupRequest {

    private AddressDto address;

    private BankAccountDto bankAccountDto;

    private String commerceRegistrationNumber;
}
