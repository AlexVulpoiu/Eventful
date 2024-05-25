package com.unibuc.fmi.eventful.services;

import com.unibuc.fmi.eventful.enums.EventStatus;
import com.unibuc.fmi.eventful.model.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SendEmailService {

    @Value("${eventful.app.frontend.url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${eventful.app.name}")
    private String senderName;

    private final JavaMailSender javaMailSender;

    public void sendVerificationEmail(AbstractUser user) throws MessagingException, UnsupportedEncodingException {
        String toAddress = user.getEmail();
        String subject = "Please verify your registration";
        String content = "Dear [[name]],<br><br>"
                + "Please click the link below to verify your registration:<br>"
                + "<h3><a href=\"[[URL]]\" target=\"_self\">VERIFY</a></h3>"
                + "Thank you,<br>"
                + senderName;

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(senderEmail, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);

        content = content.replace("[[name]]", user.getFullName());
        String verifyURL = frontendUrl + "/verify?code=" + user.getVerificationCode();

        content = content.replace("[[URL]]", verifyURL);

        helper.setText(content, true);

        javaMailSender.send(message);
    }

    public void sendEventStatusChangedEmail(Event event) throws MessagingException, UnsupportedEncodingException {
        String toAddress = event.getOrganiser().getEmail();
        String subject = "Status changed for " + event.getName();
        String reason = EventStatus.REJECTED.equals(event.getStatus())
                ? "The reason for this decision is " + event.getRejectionReason() + ".<br>" : "";
        String content = "Hello [[NAME]],<br><br>"
                + "Your event - [[EVENT_NAME]] was [[STATUS]] by one of Eventful admins.<br>"
                + reason
                + "<br>Thank you,<br>"
                + senderName;

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(senderEmail, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);

        content = content.replace("[[NAME]]", event.getOrganiser().getFullName());
        content = content.replace("[[EVENT_NAME]]", event.getName());
        content = content.replace("[[STATUS]]", String.valueOf(event.getStatus()));

        helper.setText(content, true);

        javaMailSender.send(message);
    }

    public void sendOrderSummaryEmail(Order order, Map<String, ByteArrayDataSource> pdfTickets, ByteArrayDataSource ics)
            throws MessagingException, UnsupportedEncodingException {
        String toAddress = order.getUser().getEmail();
        String subject = "Eventful order " + order.getId() + " summary";
        String content = "Hello [[NAME]],<br>"
                + "<br>Here is the summary for order number [[ORDER_NUMBER]] from Eventful.<br>"
                + """
                    <table style="text-align:center; border:1px solid black; border-collapse:collapse;">
                        <thead>
                            <tr>
                                <th style="width: 55%; border:1px solid black; border-collapse:collapse;">PRODUCT</th>
                                <th style="width: 15%; border:1px solid black; border-collapse:collapse;">QUANTITY</th>
                                <th style="width: 15%; border:1px solid black; border-collapse:collapse;">PRICE</th>
                                <th style="width: 15%; border:1px solid black; border-collapse:collapse;">TOTAL</th>
                            </tr>
                        </thead>
                        <tbody>
                            [[TABLE_BODY]]
                        </tbody>
                    </table>
                """
                + "<br>You can find the tickets in the attachments of this email.<br>"
                + "<br>Thank you,<br>"
                + senderName;

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(senderEmail, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);

        content = content.replace("[[NAME]]", order.getUser().getFullName());
        content = content.replace("[[ORDER_NUMBER]]", String.valueOf(order.getId()));

        StringBuilder tableBody = new StringBuilder();
        if (order.getEvent().getLocation() instanceof SeatedLocation) {
            Map<String, List<SeatedTicket>> ticketsByCategory = new HashMap<>();
            for (var ticket : order.getTickets()) {
                var categoryName = ((SeatedTicket) ticket).getCategoryPrice().getCategory().getName();
                if (ticketsByCategory.containsKey(categoryName)) {
                    ticketsByCategory.get(categoryName).add((SeatedTicket) ticket);
                } else {
                    List<SeatedTicket> seatedTickets = new ArrayList<>();
                    seatedTickets.add((SeatedTicket) ticket);
                    ticketsByCategory.put(categoryName, seatedTickets);
                }
            }

            for (Map.Entry<String, List<SeatedTicket>> entry : ticketsByCategory.entrySet()) {
                var tableRow = getTableRowForSeatedTickets(entry);
                tableBody.append(tableRow);
            }
        } else {
            Map<String, List<StandingTicket>> ticketsByCategory = new HashMap<>();
            for (var ticket : order.getTickets()) {
                var categoryName = ((StandingTicket) ticket).getTicketPhase().getStandingCategory().getId().getName();
                if (ticketsByCategory.containsKey(categoryName)) {
                    ticketsByCategory.get(categoryName).add((StandingTicket) ticket);
                } else {
                    List<StandingTicket> standingTickets = new ArrayList<>();
                    standingTickets.add((StandingTicket) ticket);
                    ticketsByCategory.put(categoryName, standingTickets);
                }
            }

            for (Map.Entry<String, List<StandingTicket>> entry : ticketsByCategory.entrySet()) {
                var tableRow = getTableRowForStandingTickets(entry);
                tableBody.append(tableRow);
            }
        }

        String totalRow = """
                <tr>
                    <td style="border:1px solid black; border-collapse:collapse;"><span style="font-weight:bold;">ORDER TOTAL</span></td>
                    <td style="border:1px solid black; border-collapse:collapse;"></td>
                    <td style="border:1px solid black; border-collapse:collapse;"></td>
                    <td style="border:1px solid black; border-collapse:collapse;"><span style="font-weight:bold;">[[TOTAL]]</span></td>
                </tr>
                """;
        totalRow = totalRow.replace("[[TOTAL]]", String.valueOf(order.getTotal()));
        tableBody.append(totalRow);

        content = content.replace("[[TABLE_BODY]]", tableBody);
        helper.setText(content, true);

        pdfTickets.forEach((pdfName, pdf) -> {
            try {
                helper.addAttachment(pdfName, pdf);
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        });

        try {
            helper.addAttachment("event.ics", ics);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

        javaMailSender.send(message);
    }

    private static String getTableRowForSeatedTickets(Map.Entry<String, List<SeatedTicket>> entry) {
        List<SeatedTicket> ticketsList = entry.getValue();
        var tableRow = """
                <tr>
                    <td style="border:1px solid black; border-collapse:collapse;">[[PRODUCT]]</td>
                    <td style="border:1px solid black; border-collapse:collapse;">[[QUANTITY]]</td>
                    <td style="border:1px solid black; border-collapse:collapse;">[[PRICE]]</td>
                    <td style="border:1px solid black; border-collapse:collapse;">[[TOTAL]]</td>
                </tr>
                """;
        var price = ticketsList.get(0).getCategoryPrice().getPrice();
        StringBuilder productName = new StringBuilder("Individual tickets for category " + entry.getKey());
        for (var ticket : ticketsList) {
            productName.append("<br>").append(ticket.getName());
        }
        tableRow = tableRow.replace("[[PRODUCT]]", productName.toString());
        tableRow = tableRow.replace("[[QUANTITY]]", String.valueOf(ticketsList.size()));
        tableRow = tableRow.replace("[[PRICE]]", String.valueOf(price));
        tableRow = tableRow.replace("[[TOTAL]]", String.valueOf(ticketsList.size() * price));
        return tableRow;
    }

    private static String getTableRowForStandingTickets(Map.Entry<String, List<StandingTicket>> entry) {
        var ticketsList = entry.getValue();
        var tableRow = """
                <tr>
                    <td style="border:1px solid black; border-collapse:collapse;">[[PRODUCT]]</td>
                    <td style="border:1px solid black; border-collapse:collapse;">[[QUANTITY]]</td>
                    <td style="border:1px solid black; border-collapse:collapse;">[[PRICE]]</td>
                    <td style="border:1px solid black; border-collapse:collapse;">[[TOTAL]]</td>
                </tr>
                """;
        var price = ticketsList.get(0).getTicketPhase().getPrice();
        tableRow = tableRow.replace("[[PRODUCT]]", ticketsList.get(0).getName());
        tableRow = tableRow.replace("[[QUANTITY]]", String.valueOf(ticketsList.size()));
        tableRow = tableRow.replace("[[PRICE]]", String.valueOf(price));
        tableRow = tableRow.replace("[[TOTAL]]", String.valueOf(ticketsList.size() * price));
        return tableRow;
    }
}
