package com.unibuc.fmi.eventful.mappers;

import com.unibuc.fmi.eventful.dto.CharitableCauseDto;
import com.unibuc.fmi.eventful.dto.request.charitablecause.AddCharitableCauseDto;
import com.unibuc.fmi.eventful.model.CharitableCause;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CharitableCauseMapper {

    CharitableCause addCharitableCauseDtoToCharitableCause(AddCharitableCauseDto addCharitableCauseDto);

    CharitableCauseDto charitableCauseToCharitableCauseDto(CharitableCause charitableCause);
}
