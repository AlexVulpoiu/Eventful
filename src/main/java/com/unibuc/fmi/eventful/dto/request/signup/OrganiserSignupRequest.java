package com.unibuc.fmi.eventful.dto.request.signup;


import com.unibuc.fmi.eventful.dto.AddressDto;
import com.unibuc.fmi.eventful.dto.BankAccountDto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class OrganiserSignupRequest extends BaseSignupRequest {

    @NotNull
    protected AddressDto address;

    @NotNull
    protected BankAccountDto bankAccount;

    @NotNull
    protected String commerceRegistrationNumber;
}
