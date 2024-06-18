package com.unibuc.fmi.eventful.dto;

import com.unibuc.fmi.eventful.enums.OrganiserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganiserProfileDto {

    private Long id;

    private String name;

    private String email;

    private String phone;

    private OrganiserStatus status;

    private AddressDto address;

    private BankAccountDto bankAccount;

    private String commerceRegistrationNumber;

    private Long cnp;

    private Long cui;

    private String legalName;
}
