package com.unibuc.fmi.eventful.mappers;

import com.unibuc.fmi.eventful.dto.AddressDto;
import com.unibuc.fmi.eventful.dto.BankAccountDto;
import com.unibuc.fmi.eventful.dto.request.signup.LegalPersonSignupRequest;
import com.unibuc.fmi.eventful.dto.request.signup.PersonSignupRequest;
import com.unibuc.fmi.eventful.model.Address;
import com.unibuc.fmi.eventful.model.BankAccount;
import com.unibuc.fmi.eventful.model.LegalPerson;
import com.unibuc.fmi.eventful.model.Person;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrganiserMapper {

    @Mapping(target = "commerceRegistrationNumber", source = "request.commerceRegistrationNumber")
    @Mapping(target = "address", source = "request.address", defaultExpression = "java(addressDtoToAddress(request.getAddress()))")
    @Mapping(target = "bankAccount", source = "request.bankAccount", defaultExpression = "java(bankAccountDtoToBankAccount(request.getBankAccount()))")
    LegalPerson legalPersonSignupRequestToLegalPerson(LegalPersonSignupRequest request);

    @Mapping(target = "commerceRegistrationNumber", source = "request.commerceRegistrationNumber")
    @Mapping(target = "address", source = "request.address", defaultExpression = "java(addressDtoToAddress(request.getAddress()))")
    @Mapping(target = "bankAccount", source = "request.bankAccount", defaultExpression = "java(bankAccountDtoToBankAccount(request.getBankAccount()))")
    Person personSignupRequestToPerson(PersonSignupRequest request);

    BankAccount bankAccountDtoToBankAccount(BankAccountDto dto);

    Address addressDtoToAddress(AddressDto dto);
}
