package com.unibuc.fmi.eventful.services;

import com.unibuc.fmi.eventful.dto.OrderDto;
import com.unibuc.fmi.eventful.dto.request.order.NewOrderDto;
import com.unibuc.fmi.eventful.exceptions.BadRequestException;
import com.unibuc.fmi.eventful.exceptions.NotFoundException;
import com.unibuc.fmi.eventful.mappers.OrderMapper;
import com.unibuc.fmi.eventful.model.*;
import com.unibuc.fmi.eventful.model.ids.CategoryPriceId;
import com.unibuc.fmi.eventful.model.ids.StandingCategoryId;
import com.unibuc.fmi.eventful.repository.*;
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

    CategoryPriceRepository categoryPriceRepository;
    EventRepository eventRepository;
    OrderRepository orderRepository;
    SeatedTicketRepository seatedTicketRepository;
    SeatsCategoryRepository seatsCategoryRepository;
    StandingCategoryRepository standingCategoryRepository;
    StandingTicketRepository standingTicketRepository;
    UserRepository userRepository;
    OrderMapper orderMapper;

    @Transactional
    public OrderDto placeOrder(NewOrderDto newOrderDto, Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found!"));
        var event = eventRepository.findById(newOrderDto.getEventId())
                .orElseThrow(() -> new NotFoundException("Event with id " + newOrderDto.getEventId() + " not found!"));
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
                var standingCategoryId = new StandingCategoryId(location.getId(), event.getId(), entry.getKey());
                var standingCategory = standingCategoryRepository.findById(standingCategoryId)
                        .orElseThrow(() -> new NotFoundException("Some of the categories don't exist!"));

                var availableTickets = standingCategory.getCapacity() - standingCategory.getSoldTickets();
                if (availableTickets < entry.getValue()) {
                    throw new BadRequestException("Only " + availableTickets + " tickets available for " + entry.getKey() + "category!");
                }

                var currentTicketPhase = standingCategory.getCurrentTicketPhase();
                for (int i = 0; i < entry.getValue(); i++) {
                    var standingTicket = new StandingTicket(currentTicketPhase);
                    tickets.add(standingTicketRepository.save(standingTicket));
                    total += currentTicketPhase.getPrice();
                }
            }
        } else {
            var seatedTicketsDetails = newOrderDto.getSeatedTicketsDetails();
            if (seatedTicketsDetails == null || seatedTicketsDetails.isEmpty()) {
                throw new BadRequestException("The seats must be selected!");
            }

            for (var seatDetails : seatedTicketsDetails) {
                var categoryPriceId = new CategoryPriceId(seatDetails.getCategoryId(), event.getId());
                var categoryPrice = categoryPriceRepository.findById(categoryPriceId)
                        .orElseThrow(() -> new NotFoundException("Some of the price categories don't exist!"));
                var seatsCategory = seatsCategoryRepository.findById(seatDetails.getCategoryId())
                        .orElseThrow(() -> new NotFoundException("Some of the seated categories don't exist!"));

                if (seatDetails.getRow() < seatsCategory.getMinRow() || seatsCategory.getMaxRow() < seatDetails.getRow()
                        || seatDetails.getSeat() < seatsCategory.getMinSeat() || seatsCategory.getMaxSeat() < seatDetails.getSeat()) {
                    throw new BadRequestException("Some of the selected seats are not part of the selected category!");
                }

                if (seatedTicketRepository.findByRowAndSeatAndCategoryAndEvent(seatDetails.getRow(),
                        seatDetails.getSeat(), seatDetails.getCategoryId(), event.getId()).isPresent()) {
                    throw new BadRequestException("Some of the selected seats are already sold!");
                }

                var seatedTicket = new SeatedTicket(seatDetails.getRow(), seatDetails.getSeat(), categoryPrice);
                tickets.add(seatedTicketRepository.save(seatedTicket));
                total += categoryPrice.getPrice();
            }
        }

        order.setTotal(total);
        order.setTickets(tickets);
        order = orderRepository.save(order);

        return orderMapper.orderToOrderDto(order);
    }
}
