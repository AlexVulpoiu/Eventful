package com.unibuc.fmi.eventful.services;

import com.unibuc.fmi.eventful.dto.OrderDetailsDto;
import com.unibuc.fmi.eventful.dto.ProfileDto;
import com.unibuc.fmi.eventful.dto.TicketDetailsDto;
import com.unibuc.fmi.eventful.exceptions.NotFoundException;
import com.unibuc.fmi.eventful.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileService {

    UserRepository userRepository;
    S3Service s3Service;

    public ProfileDto getProfileDetails(Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found!"));

        ProfileDto profileDto = ProfileDto.builder()
                .name(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .xp(user.getXp())
                .availablePoints(user.getAvailablePoints())
                .build();

        var userOrders = user.getOrders().stream().sorted((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate())).toList();
        List<OrderDetailsDto> orders = new ArrayList<>();
        for (var order: userOrders) {
            List<TicketDetailsDto> tickets = new ArrayList<>();
            for (var ticket: order.getTickets()) {
                tickets.add(TicketDetailsDto.builder()
                        .name(ticket.getExternalId())
                        .url(s3Service.getObjectUrl(S3Service.TICKETS_FOLDER, ticket.getExternalId() + ".pdf"))
                        .build());
            }
            orders.add(OrderDetailsDto.builder()
                    .id(order.getId())
                    .status(order.getStatus())
                    .eventName(order.getEvent().getName())
                    .orderDate(order.getOrderDate())
                    .total(order.getTotal())
                    .tickets(tickets)
                    .build());
        }

        profileDto.setOrders(orders);

        return profileDto;
    }
}
