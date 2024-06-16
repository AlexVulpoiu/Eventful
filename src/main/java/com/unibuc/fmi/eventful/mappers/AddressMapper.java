package com.unibuc.fmi.eventful.mappers;

import com.unibuc.fmi.eventful.dto.AddressDto;
import com.unibuc.fmi.eventful.model.Address;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    AddressDto addressToAddressDto(Address address);
}
