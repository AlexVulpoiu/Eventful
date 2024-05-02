package com.unibuc.fmi.eventful.mappers;

import com.unibuc.fmi.eventful.dto.SeatedLocationDto;
import com.unibuc.fmi.eventful.dto.SeatsCategoryDto;
import com.unibuc.fmi.eventful.dto.StandingLocationDto;
import com.unibuc.fmi.eventful.dto.request.location.AddSeatedLocationDto;
import com.unibuc.fmi.eventful.dto.request.location.AddSeatsCategoryDto;
import com.unibuc.fmi.eventful.dto.request.location.AddStandingLocationDto;
import com.unibuc.fmi.eventful.model.SeatedLocation;
import com.unibuc.fmi.eventful.model.SeatsCategory;
import com.unibuc.fmi.eventful.model.StandingLocation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    StandingLocation addStandingLocationDtoToStandingLocation(AddStandingLocationDto addStandingLocationDto);

    @Mapping(target = "seatsCategories", source = "addSeatedLocationDto.seatsCategories",
            defaultExpression = "java(addSeatsCategoryDtoListToSeatsCategoryList(addSeatedLocationDto.getSeatsCategories()))")
    SeatedLocation addSeatedLocationDtoToSeatedLocation(AddSeatedLocationDto addSeatedLocationDto);

    StandingLocationDto standingLocationToDto(StandingLocation standingLocation);

    @Mapping(target = "seatsCategories", source = "seatedLocation.seatsCategories",
            defaultExpression = "java(seatsCategoriesToSeatsCategoryDtoList(seatedLocation.getSeatsCategories()))")
    SeatedLocationDto seatedLocationToDto(SeatedLocation seatedLocation);

    List<SeatsCategory> addSeatsCategoryDtoListToSeatsCategoryList(List<AddSeatsCategoryDto> addSeatsCategoryDtoList);

    List<SeatsCategoryDto> seatsCategoriesToSeatsCategoryDtoList(List<SeatsCategory> seatsCategories);
}
