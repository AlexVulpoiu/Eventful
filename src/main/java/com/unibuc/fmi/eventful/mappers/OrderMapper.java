package com.unibuc.fmi.eventful.mappers;

import com.unibuc.fmi.eventful.dto.OrderDto;
import com.unibuc.fmi.eventful.model.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderDto orderToOrderDto(Order order);
}
