package com.unibuc.fmi.eventful.mappers;

import com.unibuc.fmi.eventful.dto.BankAccountDto;
import com.unibuc.fmi.eventful.model.BankAccount;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BankAccountMapper {

    BankAccountDto bankAccountToBankAccountDto(BankAccount bankAccount);
}
