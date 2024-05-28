package com.unibuc.fmi.eventful.services;

import com.unibuc.fmi.eventful.dto.OrderDto;
import com.unibuc.fmi.eventful.dto.request.order.NewOrderDto;
import com.unibuc.fmi.eventful.enums.EventStatus;
import com.unibuc.fmi.eventful.exceptions.BadRequestException;
import com.unibuc.fmi.eventful.exceptions.NotFoundException;
import com.unibuc.fmi.eventful.mappers.OrderMapper;
import com.unibuc.fmi.eventful.model.*;
import com.unibuc.fmi.eventful.repository.EventRepository;
import com.unibuc.fmi.eventful.repository.OrderRepository;
import com.unibuc.fmi.eventful.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderService {

    EventRepository eventRepository;
    OrderRepository orderRepository;
    UserRepository userRepository;
    TicketService ticketService;
    OrderMapper orderMapper;

    @Transactional
    public OrderDto placeOrder(NewOrderDto newOrderDto, Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found!"));
        if (user.getAvailablePoints() < newOrderDto.getDiscountPoints()) {
            throw new BadRequestException("You don't have enough discount points!");
        }

        var event = eventRepository.findById(newOrderDto.getEventId())
                .orElseThrow(() -> new NotFoundException("Event with id " + newOrderDto.getEventId() + " not found!"));
        if (!EventStatus.ACCEPTED.equals(event.getStatus())) {
            throw new BadRequestException("You can't place orders for this event yet!");
        }
        var location = event.getLocation();

        var order = orderRepository.save(new Order(user, event));

        var total = 0.0;
        List<AbstractTicket> tickets = new ArrayList<>();
        if (location instanceof StandingLocation) {
            var standingTickets = newOrderDto.getStandingTickets();
            if (standingTickets == null || standingTickets.isEmpty()) {
                throw new BadRequestException("The tickets categories must be provided!");
            }

            for (var entry : standingTickets.entrySet()) {
                tickets.addAll(ticketService.generateStandingTicketsByEventAndLocationAndCategory(entry.getValue(),
                        location.getId(), event.getId(), entry.getKey(), order));
                total += tickets.stream()
                        .map(ticket -> ((StandingTicket) ticket).getTicketPhase().getPrice())
                        .reduce(0.0, Double::sum);
            }
        } else {
            var seatedTicketsDetails = newOrderDto.getSeatedTicketsDetails();
            if (seatedTicketsDetails == null || seatedTicketsDetails.isEmpty()) {
                throw new BadRequestException("The seats must be selected!");
            }

            tickets = ticketService.generateSeatedTicketsByEvent(seatedTicketsDetails, event.getId(), order);
            total = tickets.stream()
                    .map(ticket -> ((SeatedTicket) ticket).getCategoryPrice().getPrice())
                    .reduce(0.0, Double::sum);
        }

        if (total - 1.0 * newOrderDto.getDiscountPoints() / 10.0 < 0.0) {
            throw new BadRequestException("Discount points value exceeds order total!");
        }
        order.setDiscountPoints(newOrderDto.getDiscountPoints());
        order.setTotal(total);
        order.setTickets(tickets);
        order = orderRepository.save(order);

        user.usePoints(order.getDiscountPoints());
        userRepository.save(user);

        return orderMapper.orderToOrderDto(order);
    }
}
