package com.unibuc.fmi.eventful.mappers;

import com.unibuc.fmi.eventful.dto.EventDto;
import com.unibuc.fmi.eventful.dto.EventPreviewDto;
import com.unibuc.fmi.eventful.dto.request.event.AddEventDto;
import com.unibuc.fmi.eventful.model.AbstractLocation;
import com.unibuc.fmi.eventful.model.Event;
import com.unibuc.fmi.eventful.model.Organiser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "categoryPrices", ignore = true)
    @Mapping(target = "standingCategories", ignore = true)
    Event addEventDtoToEvent(AddEventDto addEventDto);

    @Mapping(target = "location", source = "location", qualifiedByName = "getShortAddressWithName")
    EventPreviewDto eventToEventPreviewDto(Event event);

    @Mapping(target = "location", ignore = true)
    @Mapping(target = "standingCategories", ignore = true)
    @Mapping(target = "seatsCategories", ignore = true)
    @Mapping(target = "unavailableSeats", ignore = true)
    @Mapping(target = "raffle", ignore = true)
    @Mapping(target = "organiserName", source = "organiser", qualifiedByName = "getOrganiserName")
    EventDto eventToEventDto(Event event);

    @Named("getShortAddressWithName")
    static String getShortAddressWithName(AbstractLocation location) {
        return location.getShortAddressWithName();
    }

    @Named("getOrganiserName")
    static String getOrganiserName(Organiser organiser) {
        return organiser.getOrganiserName();
    }
}
