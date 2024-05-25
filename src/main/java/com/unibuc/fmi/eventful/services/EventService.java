package com.unibuc.fmi.eventful.services;

import com.unibuc.fmi.eventful.dto.request.event.AddEventDto;
import com.unibuc.fmi.eventful.dto.request.event.ChangeEventStatusDto;
import com.unibuc.fmi.eventful.enums.EventStatus;
import com.unibuc.fmi.eventful.enums.FeeSupporter;
import com.unibuc.fmi.eventful.exceptions.BadRequestException;
import com.unibuc.fmi.eventful.exceptions.ForbiddenException;
import com.unibuc.fmi.eventful.exceptions.NotFoundException;
import com.unibuc.fmi.eventful.mappers.EventMapper;
import com.unibuc.fmi.eventful.model.*;
import com.unibuc.fmi.eventful.model.ids.CategoryPriceId;
import com.unibuc.fmi.eventful.model.ids.StandingCategoryId;
import com.unibuc.fmi.eventful.repository.*;
import com.unibuc.fmi.eventful.utils.FileUploadUtils;
import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventService {

    @Value("${eventful.app.images.directory}")
    private String imagesDirectory;
    @Value("${eventful.app.fee}")
    private int fee;

    final AbstractLocationRepository abstractLocationRepository;
    final CategoryPriceRepository categoryPriceRepository;
    final CharitableCauseRepository charitableCauseRepository;
    final EventRepository eventRepository;
    final OrganiserRepository organiserRepository;
    final SeatsCategoryRepository seatsCategoryRepository;
    final StandingCategoryRepository standingCategoryRepository;
    final TicketPhaseRepository ticketPhaseRepository;
    final SendEmailService sendEmailService;
    final EventMapper eventMapper;

    @Transactional
    public Event addEvent(AddEventDto addEventDto, Long organiserId) {
        Organiser organiser = organiserRepository.findById(organiserId)
                .orElseThrow(() -> new NotFoundException("Organiser with id " + organiserId + " not found!"));
        AbstractLocation location = abstractLocationRepository.findById(addEventDto.getLocationId())
                .orElseThrow(() -> new NotFoundException("Location with id " + addEventDto.getLocationId() + " not found!"));

        if (addEventDto.getEndDate().isBefore(addEventDto.getStartDate())) {
            throw new BadRequestException("End date can't be before start date!");
        }

        if (!isLocationAvailableBetweenDates(location, addEventDto.getStartDateWithPreparationTime(),
                addEventDto.getEndDateWithPreparationTime())) {
            throw new BadRequestException("The selected location is not available at the requested date and time!");
        }

        var event = eventMapper.addEventDtoToEvent(addEventDto);
        event.setStatus(EventStatus.PENDING);
        event.setLocation(location);
        event.setOrganiser(organiser);

        if (addEventDto.getCharityPercentage() > 0) {
            if (addEventDto.getCharitableCauseId() == null) {
                throw new BadRequestException("A charitable cause must be provided for this event!");
            }
            CharitableCause charitableCause = charitableCauseRepository.findById(addEventDto.getCharitableCauseId())
                    .orElseThrow(() -> new NotFoundException("Charitable cause with id " + addEventDto.getCharitableCauseId() + " not found!"));
            event.setCharitableCause(charitableCause);
        }
        event = eventRepository.save(event);

        if (location instanceof StandingLocation) {
            var standingCategories = addEventDto.getStandingCategories();
            if (standingCategories == null || standingCategories.isEmpty()) {
                throw new BadRequestException("At least one standing category must be provided for this kind of event!");
            }

            for (var standingCategoryDto : standingCategories) {
                var standingCategoryId = new StandingCategoryId(addEventDto.getLocationId(), event.getId(),
                        standingCategoryDto.getName());
                var standingCategory = new StandingCategory(standingCategoryId, standingCategoryDto.getCapacity(),
                        (StandingLocation) location, event);
                standingCategory = standingCategoryRepository.save(standingCategory);

                for (var ticketPhaseDto : standingCategoryDto.getTicketPhases()) {
                    var ticketPhase = new TicketPhase(ticketPhaseDto.getName(),
                            computePrice(ticketPhaseDto.getPrice(), addEventDto.getFeeSupporter()),
                            ticketPhaseDto.getDateLimit(), standingCategory);
                    ticketPhaseRepository.save(ticketPhase);
                }
            }
        } else if (location instanceof SeatedLocation) {
            var categoriesPrices = addEventDto.getCategoriesPrices();
            if (categoriesPrices == null || categoriesPrices.isEmpty()) {
                throw new BadRequestException("Categories prices must be provided for this kind of event!");
            }

            for (var categoryPriceDto : categoriesPrices) {
                var seatsCategoryId = categoryPriceDto.getCategoryId();
                var seatsCategory = seatsCategoryRepository.findById(seatsCategoryId)
                        .orElseThrow(() -> new NotFoundException("Seats category with id " + seatsCategoryId + " not found!"));

                var categoryPriceId = new CategoryPriceId(seatsCategoryId, event.getId());
                var categoryPrice = new CategoryPrice(categoryPriceId,
                        computePrice(categoryPriceDto.getPrice(), addEventDto.getFeeSupporter()), seatsCategory, event);
                categoryPriceRepository.save(categoryPrice);
            }
        }

        return event;
    }

    public void updateLogo(long eventId, MultipartFile logo, long organiserId) throws IOException {
        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " not found!"));
        if (!event.getOrganiser().getId().equals(organiserId)) {
            throw new ForbiddenException("You are not allowed to perform this action!");
        }

        if (Optional.ofNullable(logo).isPresent()) {
            String fileName = Optional.ofNullable(event.getLogo()).isPresent()
                    ? event.getLogo() : String.valueOf(UUID.randomUUID());
            String logoName = logo.getOriginalFilename();
            String extension = logoName != null ? logoName.substring(logoName.lastIndexOf('.') + 1) : "";
            fileName = String.join(".", fileName, extension);
            FileUploadUtils.uploadFile(imagesDirectory, fileName, logo);
            event.setLogo(fileName);
        }
        eventRepository.save(event);
    }

    public Event updateStatus(long eventId, ChangeEventStatusDto changeEventStatusDto)
            throws MessagingException, UnsupportedEncodingException {
        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " not found!"));

        if (EventStatus.REJECTED.equals(changeEventStatusDto.getStatus())
                && (changeEventStatusDto.getReason() == null || changeEventStatusDto.getReason().isBlank())) {
            throw new BadRequestException("A rejection reason must be provided!");
        }
        event.setStatus(changeEventStatusDto.getStatus());
        if (EventStatus.REJECTED.equals(changeEventStatusDto.getStatus())) {
            event.setRejectionReason(changeEventStatusDto.getReason());
        } else {
            event.setRejectionReason(null);
        }
        event = eventRepository.save(event);
        sendEmailService.sendEventStatusChangedEmail(event);

        return event;
    }

    public String getEventLogoLocation(Event event) {
        return Paths.get(imagesDirectory, event.getLogo()).toString();
    }

    private double computePrice(int price, FeeSupporter feeSupporter) {
        return FeeSupporter.CLIENT.equals(feeSupporter) ? price + (double) fee * price / 100 : price;
    }

    private boolean isLocationAvailableBetweenDates(AbstractLocation location, LocalDateTime startDate, LocalDateTime endDate) {
        return location.getEvents().stream()
                .filter(event -> startDate.isBefore(event.getEndDateWithPreparationTime())
                        && endDate.isAfter(event.getStartDateWithPreparationTime()))
                .toList().isEmpty();
    }
}
