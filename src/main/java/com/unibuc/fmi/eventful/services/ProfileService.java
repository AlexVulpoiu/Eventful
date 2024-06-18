package com.unibuc.fmi.eventful.services;

import com.unibuc.fmi.eventful.dto.OrderDetailsDto;
import com.unibuc.fmi.eventful.dto.OrganiserProfileDto;
import com.unibuc.fmi.eventful.dto.ProfileDto;
import com.unibuc.fmi.eventful.dto.TicketDetailsDto;
import com.unibuc.fmi.eventful.enums.PaymentIntentStatus;
import com.unibuc.fmi.eventful.exceptions.NotFoundException;
import com.unibuc.fmi.eventful.mappers.AddressMapper;
import com.unibuc.fmi.eventful.mappers.BankAccountMapper;
import com.unibuc.fmi.eventful.model.LegalPerson;
import com.unibuc.fmi.eventful.model.Person;
import com.unibuc.fmi.eventful.repository.OrganiserRepository;
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

    AddressMapper addressMapper;
    BankAccountMapper bankAccountMapper;
    OrganiserRepository organiserRepository;
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
            if (String.valueOf(PaymentIntentStatus.SUCCEEDED).equals(order.getStatus())) {
                for (var ticket : order.getTickets()) {
                    tickets.add(TicketDetailsDto.builder()
                            .name(ticket.getExternalId())
                            .url(s3Service.getObjectUrl(S3Service.TICKETS_FOLDER, ticket.getExternalId() + ".pdf"))
                            .build());
                }
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

    public int getAvailablePoints(Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found!"));
        return user.getAvailablePoints();
    }

    public OrganiserProfileDto getOrganiserProfile(Long organiserId) {
        var organiser = organiserRepository.findById(organiserId)
                .orElseThrow(() -> new NotFoundException("Organiser with id " + organiserId + " not found!"));

        OrganiserProfileDto organiserProfileDto = OrganiserProfileDto.builder()
                .id(organiserId)
                .name(organiser.getFullName())
                .email(organiser.getEmail())
                .phone(organiser.getPhone())
                .status(organiser.getStatus())
                .address(addressMapper.addressToAddressDto(organiser.getAddress()))
                .bankAccount(bankAccountMapper.bankAccountToBankAccountDto(organiser.getBankAccount()))
                .commerceRegistrationNumber(organiser.getCommerceRegistrationNumber())
                .build();

        if (organiser instanceof LegalPerson) {
            organiserProfileDto.setCui(((LegalPerson) organiser).getCui());
            organiserProfileDto.setLegalName(((LegalPerson) organiser).getName());
        } else {
            organiserProfileDto.setCnp(((Person) organiser).getCnp());
        }

        return organiserProfileDto;
    }
}
