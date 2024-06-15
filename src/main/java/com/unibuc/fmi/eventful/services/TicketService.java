package com.unibuc.fmi.eventful.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.font.FontProvider;
import com.unibuc.fmi.eventful.dto.TicketDto;
import com.unibuc.fmi.eventful.dto.request.order.NewOrderDto;
import com.unibuc.fmi.eventful.exceptions.BadRequestException;
import com.unibuc.fmi.eventful.exceptions.ForbiddenException;
import com.unibuc.fmi.eventful.exceptions.NotFoundException;
import com.unibuc.fmi.eventful.model.AbstractTicket;
import com.unibuc.fmi.eventful.model.Order;
import com.unibuc.fmi.eventful.model.SeatedTicket;
import com.unibuc.fmi.eventful.model.StandingTicket;
import com.unibuc.fmi.eventful.model.ids.CategoryPriceId;
import com.unibuc.fmi.eventful.model.ids.StandingCategoryId;
import com.unibuc.fmi.eventful.repository.*;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketService {

    @Value("${eventful.app.images.directory}")
    String imagesDirectory;

    final AbstractTicketRepository abstractTicketRepository;
    final CategoryPriceRepository categoryPriceRepository;
    final EventRepository eventRepository;
    final OrganiserRepository organiserRepository;
    final SeatedTicketRepository seatedTicketRepository;
    final SeatsCategoryRepository seatsCategoryRepository;
    final StandingCategoryRepository standingCategoryRepository;
    final EventService eventService;
    final S3Service s3Service;
    final SendEmailService sendEmailService;
    final VoucherService voucherService;
    final TemplateEngine templateEngine;
    QRCodeWriter qrCodeWriter;
    ConverterProperties converterProperties;

    @PostConstruct
    public void init() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateEngine.setTemplateResolver(templateResolver);

        qrCodeWriter = new QRCodeWriter();

        FontProvider fontProvider = new DefaultFontProvider();
        fontProvider.addFont("src/main/resources/static/fonts/Roboto-Light.ttf");
        converterProperties = new ConverterProperties();
        converterProperties.setFontProvider(fontProvider);
    }

    @Transactional
    public List<AbstractTicket> generateStandingTicketsByEventAndLocationAndCategory(int numberOfTickets, long locationId, long eventId,
                                                                                     String category, Order order) {

        var standingCategoryId = new StandingCategoryId(locationId, eventId, category);
        var standingCategory = standingCategoryRepository.findById(standingCategoryId)
                .orElseThrow(() -> new NotFoundException("Some of the categories don't exist!"));

        var availableTickets = standingCategory.getCapacity() - standingCategory.getSoldTickets();
        if (availableTickets < numberOfTickets) {
            throw new BadRequestException("Only " + availableTickets + " tickets available for " + category + "category!");
        }

        List<AbstractTicket> tickets = new ArrayList<>();
        for (int i = 0; i < numberOfTickets; i++) {
            tickets.add(new StandingTicket(order, standingCategory));
        }

        return tickets;
    }

    @Transactional
    public List<AbstractTicket> generateSeatedTicketsByEvent(List<NewOrderDto.SeatDetails> seatedTicketsDetails,
                                                             long eventId, Order order) {
        List<AbstractTicket> tickets = new ArrayList<>();

        for (var seatDetails : seatedTicketsDetails) {
            var categoryPriceId = new CategoryPriceId(seatDetails.getCategoryId(), eventId);
            var categoryPrice = categoryPriceRepository.findById(categoryPriceId)
                    .orElseThrow(() -> new NotFoundException("Some of the price categories don't exist!"));
            var seatsCategory = seatsCategoryRepository.findById(seatDetails.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Some of the seated categories don't exist!"));

            if (seatDetails.getRow() < seatsCategory.getMinRow() || seatsCategory.getMaxRow() < seatDetails.getRow()
                    || seatDetails.getSeat() < seatsCategory.getMinSeat() || seatsCategory.getMaxSeat() < seatDetails.getSeat()) {
                throw new BadRequestException("Some of the selected seats are not part of the selected category!");
            }

            if (seatedTicketRepository.findByRowAndSeatAndCategoryAndEvent(seatDetails.getRow(),
                    seatDetails.getSeat(), seatDetails.getCategoryId(), eventId).isPresent()) {
                throw new BadRequestException("Some of the selected seats are not available!");
            }

            tickets.add(new SeatedTicket(order, seatDetails.getRow(), seatDetails.getSeat(), categoryPrice));
        }

        return tickets;
    }

    public void generatePdfTicketsAndSendOrderSummaryEmail(Order order) throws IOException, WriterException, MessagingException {
        Map<String, ByteArrayDataSource> pdfTickets = new HashMap<>();
        for (var ticket : order.getTickets()) {
            ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
            PdfWriter pdfWriter = new PdfWriter(pdfStream);
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
            Document document = new Document(pdfDocument);
            document.setMargins(0, 0, 0, 0);
            HtmlConverter.convertToPdf(new ByteArrayInputStream(parseTicketTemplate(order, ticket).getBytes()), pdfDocument, converterProperties);
            var pdfName = ticket.getExternalId() + ".pdf";
            pdfTickets.put(pdfName, new ByteArrayDataSource(pdfStream.toByteArray(), "application/pdf"));
            s3Service.uploadFile(S3Service.TICKETS_FOLDER, pdfName, new ByteArrayInputStream(pdfStream.toByteArray()));
            document.close();
        }
        sendEmailService.sendOrderSummaryEmail(order, pdfTickets, eventService.generateIcsForEvent(order.getEvent()),
                voucherService.generateVoucher(order));
    }

    public String parseTicketTemplate(Order order, AbstractTicket ticket) throws IOException, WriterException {
        Context context = new Context();
        SeatedTicket seatedTicket = ticket instanceof SeatedTicket ? (SeatedTicket) ticket : null;
        StandingTicket standingTicket = ticket instanceof StandingTicket ? (StandingTicket) ticket : null;
        context.setVariable("ticket", seatedTicket != null ? seatedTicket : standingTicket);
        context.setVariable("order", order);
        context.setVariable("eventLogo", eventService.getEventLogoUrl(order.getEvent()));
        context.setVariable("qrBarcode", generateQRBarcode(ticket));
        return templateEngine.process(ticket instanceof SeatedTicket ? "templates/seated_ticket" : "templates/standing_ticket", context);
    }

    private String generateQRBarcode(AbstractTicket ticket) throws WriterException, IOException {
        BitMatrix bitMatrix = qrCodeWriter.encode(ticket.getExternalId(), BarcodeFormat.QR_CODE, 250, 250);
        Path qrLocation = Paths.get(imagesDirectory, "qrcode_" + ticket.getExternalId() + ".png");
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", qrLocation);
        return String.valueOf(qrLocation);
    }

    public TicketDto getInfo(Long eventId, String ticketId, Long organiserId) {
        organiserRepository.findById(organiserId)
                .orElseThrow(() -> new ForbiddenException("You are not allowed to perform this operation!"));
        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " not found!"));
        if (!Objects.equals(event.getOrganiser().getId(), organiserId)) {
            throw new ForbiddenException("You are not allowed to perform this operation!");
        }

        var ticket = abstractTicketRepository.findByExternalIdAndEventId(ticketId, eventId)
                .orElseThrow(() -> new BadRequestException("There is no ticket with these details for the selected event!"));

        var ticketDto = TicketDto.builder()
                .externalId(ticket.getExternalId())
                .eventId(eventId)
                .eventName(event.getName())
                .startDate(event.getStartDate())
                .locationAddress(event.getLocation().getShortAddressWithName())
                .validated(ticket.isValidated())
                .build();

        if (ticket instanceof StandingTicket standingTicket) {
            ticketDto.setCategory(standingTicket.getStandingCategory().getId().getName());
        } else if (ticket instanceof SeatedTicket seatedTicket) {
            ticketDto.setCategory(seatedTicket.getCategoryPrice().getCategory().getName());
            ticketDto.setRow(seatedTicket.getNumberOfRow());
            ticketDto.setSeat(seatedTicket.getSeat());
        }

        return ticketDto;
    }

    public void validate(Long eventId, String ticketId, Long organiserId) {
        organiserRepository.findById(organiserId)
                .orElseThrow(() -> new ForbiddenException("You are not allowed to perform this operation!"));
        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " not found!"));
        if (!Objects.equals(event.getOrganiser().getId(), organiserId)) {
            throw new ForbiddenException("You are not allowed to perform this operation!");
        }

        var ticket = abstractTicketRepository.findByExternalIdAndEventId(ticketId, eventId)
                .orElseThrow(() -> new BadRequestException("There is no ticket with these details for the selected event!"));
        if (ticket.isValidated()) {
            throw new BadRequestException("The ticket was already validated!");
        }

        ticket.setValidated(true);
        abstractTicketRepository.save(ticket);
    }
}
