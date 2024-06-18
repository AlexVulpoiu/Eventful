package com.unibuc.fmi.eventful.services;

import com.unibuc.fmi.eventful.dto.OrganiserProfileDto;
import com.unibuc.fmi.eventful.enums.OrganiserStatus;
import com.unibuc.fmi.eventful.exceptions.BadRequestException;
import com.unibuc.fmi.eventful.exceptions.NotFoundException;
import com.unibuc.fmi.eventful.mappers.AddressMapper;
import com.unibuc.fmi.eventful.mappers.BankAccountMapper;
import com.unibuc.fmi.eventful.model.LegalPerson;
import com.unibuc.fmi.eventful.model.Person;
import com.unibuc.fmi.eventful.repository.OrganiserRepository;
import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrganiserService {

    AddressMapper addressMapper;
    BankAccountMapper bankAccountMapper;
    OrganiserRepository organiserRepository;
    SendEmailService sendEmailService;

    public List<OrganiserProfileDto> getOrganisersByStatus(OrganiserStatus status) {
        var organisers = organiserRepository.findByStatus(status);
        List<OrganiserProfileDto> organiserProfileDtoList = new ArrayList<>();

        for (var organiser : organisers) {
            OrganiserProfileDto organiserProfileDto = OrganiserProfileDto.builder()
                    .id(organiser.getId())
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

            organiserProfileDtoList.add(organiserProfileDto);
        }

        return organiserProfileDtoList;
    }

    public void updateOrganiserStatus(Long organiserId, OrganiserStatus status) throws MessagingException, UnsupportedEncodingException {
        var organiser = organiserRepository.findById(organiserId)
                .orElseThrow(() -> new NotFoundException("Organiser with id " + organiserId + " not found!"));
        if (!OrganiserStatus.PENDING.equals(organiser.getStatus())) {
            throw new BadRequestException("The status of this organiser can't be changed!");
        }

        organiser.setStatus(status);
        organiserRepository.save(organiser);
        sendEmailService.sendOrganiserStatusChangedEmail(organiser);
    }
}
