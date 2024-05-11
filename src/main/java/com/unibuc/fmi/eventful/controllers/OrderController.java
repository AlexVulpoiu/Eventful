package com.unibuc.fmi.eventful.controllers;

import com.unibuc.fmi.eventful.dto.OrderDto;
import com.unibuc.fmi.eventful.dto.request.order.NewOrderDto;
import com.unibuc.fmi.eventful.security.services.UserDetailsImpl;
import com.unibuc.fmi.eventful.services.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderController {

    OrderService orderService;

    @PostMapping
    @PreAuthorize("hasAuthority('USER')")
    public OrderDto placeOrder(@RequestBody NewOrderDto newOrderDto, @AuthenticationPrincipal UserDetailsImpl principal) {
        return orderService.placeOrder(newOrderDto, principal.getId());
    }
}
