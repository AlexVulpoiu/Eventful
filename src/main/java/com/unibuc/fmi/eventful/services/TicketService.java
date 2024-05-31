package com.unibuc.fmi.eventful.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.font.FontProvider;
import com.unibuc.fmi.eventful.dto.request.order.NewOrderDto;
import com.unibuc.fmi.eventful.exceptions.BadRequestException;
import com.unibuc.fmi.eventful.exceptions.NotFoundException;
import com.unibuc.fmi.eventful.model.AbstractTicket;
import com.unibuc.fmi.eventful.model.Order;
import com.unibuc.fmi.eventful.model.SeatedTicket;
import com.unibuc.fmi.eventful.model.StandingTicket;
import com.unibuc.fmi.eventful.model.ids.CategoryPriceId;
import com.unibuc.fmi.eventful.model.ids.StandingCategoryId;
import com.unibuc.fmi.eventful.repository.CategoryPriceRepository;
import com.unibuc.fmi.eventful.repository.SeatedTicketRepository;
import com.unibuc.fmi.eventful.repository.SeatsCategoryRepository;
import com.unibuc.fmi.eventful.repository.StandingCategoryRepository;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketService {

    @Value("${eventful.app.images.directory}")
    String imagesDirectory;

    final CategoryPriceRepository categoryPriceRepository;
    final SeatedTicketRepository seatedTicketRepository;
    final SeatsCategoryRepository seatsCategoryRepository;
    final StandingCategoryRepository standingCategoryRepository;
    final EventService eventService;
    final S3Service s3Service;
    final SendEmailService sendEmailService;
    final TemplateEngine templateEngine;
    Code128Writer code128Writer;
    QRCodeWriter qrCodeWriter;
    ConverterProperties converterProperties;

    @PostConstruct
    public void init() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateEngine.setTemplateResolver(templateResolver);

        code128Writer = new Code128Writer();
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

        var currentTicketPhase = standingCategory.getCurrentTicketPhase();
        List<AbstractTicket> tickets = new ArrayList<>();
        for (int i = 0; i < numberOfTickets; i++) {
            tickets.add(new StandingTicket(order, currentTicketPhase));
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
        sendEmailService.sendOrderSummaryEmail(order, pdfTickets, eventService.generateIcsForEvent(order.getEvent()));
    }

    public String parseTicketTemplate(Order order, AbstractTicket ticket) throws IOException, WriterException {
        Context context = new Context();
        SeatedTicket seatedTicket = ticket instanceof SeatedTicket ? (SeatedTicket) ticket : null;
        StandingTicket standingTicket = ticket instanceof StandingTicket ? (StandingTicket) ticket : null;
        context.setVariable("ticket", seatedTicket != null ? seatedTicket : standingTicket);
        context.setVariable("order", order);
        context.setVariable("eventLogo", eventService.getEventLogoUrl(order.getEvent()));
        context.setVariable("code128Barcode", generateCode128Barcode(ticket));
        context.setVariable("qrBarcode", generateQRBarcode(ticket));
        return templateEngine.process(ticket instanceof SeatedTicket ? "templates/seated_ticket" : "templates/standing_ticket", context);
    }

    private String generateCode128Barcode(AbstractTicket ticket) throws IOException {
        BitMatrix bitMatrix = code128Writer.encode(ticket.getExternalId(), BarcodeFormat.CODE_128, 650, 150);
        Path barcodeLocation = Paths.get(imagesDirectory, "code128_" + ticket.getExternalId() + ".png");
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", barcodeLocation);
        return String.valueOf(barcodeLocation);
    }

    private String generateQRBarcode(AbstractTicket ticket) throws WriterException, IOException {
        BitMatrix bitMatrix = qrCodeWriter.encode(ticket.getExternalId(), BarcodeFormat.QR_CODE, 250, 250);
        Path qrLocation = Paths.get(imagesDirectory, "qrcode_" + ticket.getExternalId() + ".png");
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", qrLocation);
        return String.valueOf(qrLocation);
    }
}
