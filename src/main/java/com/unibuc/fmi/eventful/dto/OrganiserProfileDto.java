package com.unibuc.fmi.eventful.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganiserProfileDto {

    private String name;

    private String email;

    private String phone;

    private AddressDto address;

    private BankAccountDto bankAccount;

    private String commerceRegistrationNumber;

    private Long cnp;

    private Long cui;

    private String legalName;
}
