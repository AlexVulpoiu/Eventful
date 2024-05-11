package com.unibuc.fmi.eventful.mappers;

import com.unibuc.fmi.eventful.dto.request.event.AddEventDto;
import com.unibuc.fmi.eventful.model.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "categoryPrices", ignore = true)
    @Mapping(target = "standingCategories", ignore = true)
    Event addEventDtoToEvent(AddEventDto addEventDto);
}
