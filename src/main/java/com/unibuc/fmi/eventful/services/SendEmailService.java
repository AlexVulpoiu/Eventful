package com.unibuc.fmi.eventful.services;

import com.unibuc.fmi.eventful.enums.EventStatus;
import com.unibuc.fmi.eventful.enums.OrganiserStatus;
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
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SendEmailService {

    @Value("${eventful.app.confirm.account.url}")
    private String confirmAccountUrl;

    @Value("${eventful.app.events.review.url}")
    private String eventsReviewUrl;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${eventful.app.name}")
    private String senderName;

    private final JavaMailSender javaMailSender;

    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy, HH:mm");

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
        String verifyURL = confirmAccountUrl + "/" + user.getVerificationCode();

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

    public void sendOrganiserStatusChangedEmail(Organiser organiser) throws MessagingException, UnsupportedEncodingException {
        String toAddress = organiser.getEmail();
        String subject = "Status changed for your account";
        String content = "Hello [[NAME]],<br><br>"
                + "Your organiser account was [[STATUS]] by one of Eventful admins.<br>"
                + (OrganiserStatus.REJECTED.equals(organiser.getStatus()) ? "You will be contacted soon by one of the admins." : "From now on, you can start creating events!")
                + "<br><br>Thank you,<br>"
                + senderName;

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(senderEmail, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);

        content = content.replace("[[NAME]]", organiser.getFullName());
        content = content.replace("[[STATUS]]", organiser.getStatus().name());

        helper.setText(content, true);

        javaMailSender.send(message);
    }

    public void sendOrderSummaryEmail(Order order, Map<String, ByteArrayDataSource> pdfTickets, ByteArrayDataSource ics,
                                      Voucher voucher)
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
                + "[[VOUCHER_DETAILS]]"
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
                var categoryName = ((StandingTicket) ticket).getStandingCategory().getId().getName();
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

        if (order.getDiscountPoints() > 0) {
            String discountRow = """
                <tr>
                    <td style="border:1px solid black; border-collapse:collapse;">DISCOUNT</td>
                    <td style="border:1px solid black; border-collapse:collapse;">1</td>
                    <td style="border:1px solid black; border-collapse:collapse;">[[DISCOUNT_TOTAL]]</td>
                    <td style="border:1px solid black; border-collapse:collapse;">[[DISCOUNT_TOTAL]]</td>
                </tr>
                """;
            discountRow = discountRow.replace("[[DISCOUNT_TOTAL]]", String.valueOf(-1.0 * order.getDiscountPoints() / 10.0));
            tableBody.append(discountRow);
        }

        String paymentTotalRow = """
                <tr>
                    <td style="border:1px solid black; border-collapse:collapse;"><span style="font-weight:bold;">PAYMENT TOTAL</span></td>
                    <td style="border:1px solid black; border-collapse:collapse;"></td>
                    <td style="border:1px solid black; border-collapse:collapse;"></td>
                    <td style="border:1px solid black; border-collapse:collapse;"><span style="font-weight:bold;">[[PAYMENT_TOTAL]]</span></td>
                </tr>
                """;
        paymentTotalRow = paymentTotalRow.replace("[[PAYMENT_TOTAL]]", String.valueOf(order.getPaymentAmount()));
        tableBody.append(paymentTotalRow);

        content = content.replace("[[TABLE_BODY]]", tableBody);

        String voucherDetails = "";
        if (voucher != null) {
            voucherDetails = "<br>You have won a voucher of " + voucher.getValue() + "% discount at "
                    + voucher.getName() + "! Use this code on partner's website: <strong>" + voucher.getCode() + "</string><br>";
        }

        content = content.replace("[[VOUCHER_DETAILS]]", voucherDetails);
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

    public void sendRaffleWinnerEmail(Raffle raffle, User user, Voucher voucher)
            throws MessagingException, UnsupportedEncodingException {
        String toAddress = user.getEmail();
        String subject = "Raffle winner for event: " + raffle.getEvent().getName();
        String content = "Hello [[NAME]],<br><br>"
                + "Congratulations!<br>"
                + "You are the winner of the raffle for the event - [[EVENT_NAME]]!<br>"
                + "The prize is a voucher of [[VOUCHER_VALUE]]% discount at [[PARTNER_NAME]]! "
                + "Use this code on partner's website: <strong>[[VOUCHER_CODE]]</string>.<br>"
                + "<br>Thank you,<br>"
                + senderName;

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(senderEmail, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);

        content = content.replace("[[NAME]]", user.getFullName());
        content = content.replace("[[EVENT_NAME]]", raffle.getEvent().getName());
        content = content.replace("[[VOUCHER_VALUE]]", String.valueOf(voucher.getValue()));
        content = content.replace("[[PARTNER_NAME]]", String.valueOf(voucher.getName()));
        content = content.replace("[[VOUCHER_CODE]]", String.valueOf(voucher.getCode()));

        helper.setText(content, true);

        javaMailSender.send(message);
    }

    public void sendReviewReminder(Review review) throws MessagingException, UnsupportedEncodingException {
        String toAddress = review.getUser().getEmail();
        String subject = "Review for event " + review.getEvent().getName();
        String content = "Hello [[NAME]],<br><br>"
                + "From now on, you can add a review for event [[EVENT_NAME]].<br>"
                + "Please access <a href=\"[[REVIEW_URL]]\">this URL</a> and let the organiser know your opinion on the event!<br>"
                + "Also, you will receive 10 points after sending the review.<br>"
                + "<br>Thank you,<br>"
                + senderName;

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(senderEmail, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);

        content = content.replace("[[NAME]]", review.getUser().getFullName());
        content = content.replace("[[EVENT_NAME]]", review.getEvent().getName());
        content = content.replace("[[REVIEW_URL]]", eventsReviewUrl + "/" + review.getId());

        helper.setText(content, true);

        javaMailSender.send(message);
    }

    public void sendParticipationReminder(User user, Event event) throws MessagingException, UnsupportedEncodingException {
        String toAddress = user.getEmail();
        String subject = "Participation reminder for event " + event.getName();
        String content = "Hello [[NAME]],<br><br>"
                + "You have tickets for [[EVENT_NAME]], that will start on [[EVENT_TIMESTAMP]] at [[EVENT_LOCATION]].<br>"
                + "See you there!<br>"
                + "<br>Thank you,<br>"
                + senderName;

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(senderEmail, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);

        content = content.replace("[[NAME]]", user.getFullName());
        content = content.replace("[[EVENT_NAME]]", event.getName());
        content = content.replace("[[EVENT_TIMESTAMP]]", event.getStartDate().format(DATE_TIME_FORMATTER));
        content = content.replace("[[EVENT_LOCATION]]", event.getLocation().getFullAddressWithName());

        helper.setText(content, true);

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
        var price = ticketsList.get(0).getCategoryPrice().getCurrentPrice();
        StringBuilder productName = new StringBuilder("Individual tickets for category " + entry.getKey());
        for (var ticket : ticketsList) {
            productName.append("<br>").append(ticket.getName());
        }
        tableRow = tableRow.replace("[[PRODUCT]]", productName.toString());
        tableRow = tableRow.replace("[[QUANTITY]]", String.valueOf(ticketsList.size()));
        tableRow = tableRow.replace("[[PRICE]]", String.valueOf(price));
        tableRow = tableRow.replace("[[TOTAL]]", String.valueOf(BigDecimal.valueOf(ticketsList.size()).multiply(BigDecimal.valueOf(price))));
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
        var price = ticketsList.get(0).getStandingCategory().getCurrentPrice();
        tableRow = tableRow.replace("[[PRODUCT]]", ticketsList.get(0).getName());
        tableRow = tableRow.replace("[[QUANTITY]]", String.valueOf(ticketsList.size()));
        tableRow = tableRow.replace("[[PRICE]]", String.valueOf(price));
        tableRow = tableRow.replace("[[TOTAL]]", String.valueOf(BigDecimal.valueOf(ticketsList.size()).multiply(BigDecimal.valueOf(price))));
        return tableRow;
    }
}
