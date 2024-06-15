package com.unibuc.fmi.eventful.services;

import com.unibuc.fmi.eventful.dto.EventDto;
import com.unibuc.fmi.eventful.dto.EventPreviewDto;
import com.unibuc.fmi.eventful.dto.LocationDto;
import com.unibuc.fmi.eventful.dto.RaffleDto;
import com.unibuc.fmi.eventful.dto.request.event.AddEventDto;
import com.unibuc.fmi.eventful.dto.request.event.AddPromotionDto;
import com.unibuc.fmi.eventful.dto.request.event.AddRaffleDto;
import com.unibuc.fmi.eventful.dto.request.event.ChangeEventStatusDto;
import com.unibuc.fmi.eventful.enums.EventStatus;
import com.unibuc.fmi.eventful.enums.FeeSupporter;
import com.unibuc.fmi.eventful.exceptions.BadRequestException;
import com.unibuc.fmi.eventful.exceptions.ForbiddenException;
import com.unibuc.fmi.eventful.exceptions.NotFoundException;
import com.unibuc.fmi.eventful.mappers.CharitableCauseMapper;
import com.unibuc.fmi.eventful.mappers.EventMapper;
import com.unibuc.fmi.eventful.model.*;
import com.unibuc.fmi.eventful.model.ids.CategoryPriceId;
import com.unibuc.fmi.eventful.model.ids.StandingCategoryId;
import com.unibuc.fmi.eventful.repository.*;
import jakarta.mail.MessagingException;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.TzId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.immutable.ImmutableCalScale;
import net.fortuna.ical4j.model.property.immutable.ImmutableVersion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventService {

    @Value("${eventful.app.fee}")
    private int fee;

    final AbstractLocationRepository abstractLocationRepository;
    final CategoryPriceRepository categoryPriceRepository;
    final EventRepository eventRepository;
    final OrganiserRepository organiserRepository;
    final RaffleRepository raffleRepository;
    final SeatedTicketRepository seatedTicketRepository;
    final SeatsCategoryRepository seatsCategoryRepository;
    final StandingCategoryRepository standingCategoryRepository;
    final CharitableCauseService charitableCauseService;
    final S3Service s3Service;
    final SendEmailService sendEmailService;
    final CharitableCauseMapper charitableCauseMapper;
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
            if (addEventDto.getAddCharitableCause() == null) {
                throw new BadRequestException("A charitable cause must be provided for this event!");
            }

            var charitableCause = charitableCauseService.addCharitableCause(addEventDto.getAddCharitableCause(), organiserId);
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
                        computePrice(standingCategoryDto.getPrice(), addEventDto.getFeeSupporter()),
                        (StandingLocation) location, event);
                standingCategoryRepository.save(standingCategory);
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
            String logoName = logo.getOriginalFilename();
            String extension = logoName != null ? logoName.substring(logoName.lastIndexOf('.') + 1) : "";
            String fileName = Optional.ofNullable(event.getLogo()).isPresent()
                    ? event.getLogo() : String.join(".", String.valueOf(UUID.randomUUID()), extension);
            s3Service.uploadFile(S3Service.EVENTS_FOLDER, fileName, logo.getInputStream());
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

    public URL getEventLogoUrl(Event event) {
        return s3Service.getObjectUrl(S3Service.EVENTS_FOLDER, event.getLogo());
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

    public ByteArrayDataSource generateIcsForEvent(Event event) throws IOException {
        Calendar calendar = new Calendar();
        calendar.add(new ProdId("-//Eventful//iCal4j 1.0//EN"));
        calendar.add(ImmutableVersion.VERSION_2_0);
        calendar.add(ImmutableCalScale.GREGORIAN);

        VEvent vEvent = new VEvent(event.getStartDate(), event.getEndDate(), event.getName());

        TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
        TimeZone timeZone = registry.getTimeZone("Europe/Bucharest");
        TzId tzId = new TzId(timeZone.getID());

        vEvent.add(tzId);
        vEvent.add(new Uid(String.valueOf(event.getId())));
        vEvent.add(new Location(event.getLocation().getFullAddressWithName()));

        calendar.add(vEvent);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CalendarOutputter calendarOutputter = new CalendarOutputter();
        calendarOutputter.output(calendar, baos);

        return new ByteArrayDataSource(baos.toByteArray(), "text/calendar");
    }

    public Page<EventPreviewDto> getEvents(Integer pageNumber, String search) {
        if (pageNumber == null || pageNumber < 0) {
            pageNumber = 0;
        }
        Pageable pageable = PageRequest.of(pageNumber, 9);
        var events = eventRepository.searchEventsByNameInChronologicalOrderEndingAfter(
                search == null ? "" : search.toLowerCase(), LocalDateTime.now(), pageable);

        var eventPreviews = events.getContent().stream().map(eventMapper::eventToEventPreviewDto).toList();
        eventPreviews.forEach(e -> e.setLogo(s3Service.getObjectUrl(S3Service.EVENTS_FOLDER, e.getLogo()).toString()));

        return new PageImpl<>(eventPreviews, pageable, events.getTotalElements());
    }

    public EventDto getEventDetails(Long eventId) {
        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " not found!"));
        var eventDto = eventMapper.eventToEventDto(event);

        event.getActivePromotion().ifPresent(p -> {
            eventDto.setDiscount(p.getValue());
            eventDto.setDiscountEndDate(p.getEndDate());
        });

        var raffle = event.getRaffle();
        if (raffle != null) {
            if ((raffle.getEndDate() != null && !raffle.getEndDate().isAfter(LocalDate.now()))
                    || (raffle.getParticipantsLimit() > 0 && raffle.getTotalParticipants() < raffle.getParticipantsLimit())) {
                eventDto.setRaffle(RaffleDto.builder()
                        .participantsLimit(raffle.getParticipantsLimit())
                        .endDate(raffle.getEndDate())
                        .prize(raffle.getPrize())
                        .partnerName(raffle.getPartnerName())
                        .totalParticipants(raffle.getTotalParticipants())
                        .build());
            }
        }

        if (event.getCharitableCause() != null) {
            eventDto.setCharitableCause(charitableCauseMapper.charitableCauseToCharitableCauseDto(event.getCharitableCause()));
        }

        eventDto.setLogo(s3Service.getObjectUrl(S3Service.EVENTS_FOLDER, event.getLogo()).toString());
        eventDto.setLocation(LocationDto.from(event.getLocation()));
        eventDto.setUnavailableSeats(new ArrayList<>());

        eventDto.setStandingCategories(new ArrayList<>());
        if (event.getStandingCategories() != null) {
            for (var standingCategory : event.getStandingCategories()) {
                eventDto.getStandingCategories().add(
                        EventDto.StandingCategoryDto.builder()
                                .name(standingCategory.getId().getName())
                                .price(standingCategory.getCurrentPrice())
                                .initialPrice(standingCategory.getPrice())
                                .ticketsRemaining(standingCategory.getCapacity() - standingCategory.getSoldTickets())
                                .build()
                );
            }
        }

        eventDto.setSeatsCategories(new ArrayList<>());
        if (event.getCategoryPrices() != null) {
            for (var categoryPrice : event.getCategoryPrices()) {
                eventDto.getSeatsCategories().add(
                        EventDto.SeatsCategoryDetails.builder()
                                .id(categoryPrice.getCategory().getId())
                                .name(categoryPrice.getCategory().getName())
                                .price(categoryPrice.getCurrentPrice())
                                .initialPrice(categoryPrice.getPrice())
                                .minRow(categoryPrice.getCategory().getMinRow())
                                .maxRow(categoryPrice.getCategory().getMaxRow())
                                .minSeat(categoryPrice.getCategory().getMinSeat())
                                .maxSeat(categoryPrice.getCategory().getMaxSeat())
                                .build()
                );
            }

            var soldTickets = seatedTicketRepository.findSoldTicketsByEventId(eventId);
            soldTickets.forEach(t -> eventDto.getUnavailableSeats().add(new EventDto.Seat(t.getNumberOfRow(), t.getSeat())));
        }

        return eventDto;
    }

    public EventDto addPromotion(Long eventId, AddPromotionDto promotionDto, Long organiserId) {
        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " not found!"));
        if (!event.getOrganiser().getId().equals(organiserId)) {
            throw new ForbiddenException("You are not allowed to perform this operation!");
        }

        if (event.getActivePromotion().isPresent()) {
            throw new BadRequestException("The current event has an active promotion!");
        }

        if (event.getEndDate().toLocalDate().isBefore(promotionDto.getEndDate())) {
            throw new BadRequestException("Promotion end date can't be after event end date!");
        }

        Promotion promotion = Promotion.builder()
                .value(promotionDto.getValue())
                .endDate(promotionDto.getEndDate())
                .event(event)
                .build();
        event.getPromotions().add(promotion);
        eventRepository.save(event);

        return getEventDetails(eventId);
    }

    public EventDto addRaffle(Long eventId, AddRaffleDto raffleDto, Long organiserId) {
        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " not found!"));
        if (!event.getOrganiser().getId().equals(organiserId)) {
            throw new ForbiddenException("You are not allowed to perform this operation!");
        }

        if (event.getRaffle() != null) {
            throw new BadRequestException("You can't add more than one raffle per event!");
        }

        if ((raffleDto.getParticipantsLimit() == 0 && raffleDto.getEndDate() == null)
                || (raffleDto.getParticipantsLimit() > 0 && raffleDto.getEndDate() != null)) {
            throw new BadRequestException("A raffle should have either a limit for participants or a limit date!");
        }

        Raffle raffle = Raffle.builder()
                .participantsLimit(raffleDto.getParticipantsLimit())
                .endDate(raffleDto.getEndDate())
                .prize(raffleDto.getPrize())
                .partnerName(raffleDto.getPartnerName())
                .totalParticipants(0)
                .event(event)
                .build();
        raffle = raffleRepository.save(raffle);

        event.setRaffle(raffle);
        eventRepository.save(event);

        return getEventDetails(eventId);
    }

    @Scheduled(cron = "0 0 6 ? * *")
    public void deleteRejectedEvents() {
        log.info("Starting job for rejected events deletion");
        var events = eventRepository.getRejectedEventsNotUpdatedSince(LocalDateTime.now().minusDays(7));
        log.info(events.size() + " events to delete");

        for (var e : events) {
            log.info("Deleting event with id " + e.getId());
            if (e.getLogo() != null) {
                s3Service.deleteFile(S3Service.EVENTS_FOLDER, e.getLogo());
            }
            eventRepository.delete(e);
        }
        log.info("Ending job for rejected events deletion");
    }
}
