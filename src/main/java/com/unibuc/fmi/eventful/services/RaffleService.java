package com.unibuc.fmi.eventful.services;

import com.unibuc.fmi.eventful.exceptions.NotFoundException;
import com.unibuc.fmi.eventful.repository.OrderRepository;
import com.unibuc.fmi.eventful.repository.RaffleRepository;
import com.unibuc.fmi.eventful.repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RaffleService {

    Random random = new Random();
    OrderRepository orderRepository;
    RaffleRepository raffleRepository;
    UserRepository userRepository;
    SendEmailService sendEmailService;
    VoucherService voucherService;

    @Scheduled(cron = "0 5 0 ? * *")
    public void chooseRaffleWinner() throws MessagingException, UnsupportedEncodingException {
        log.info("Starting job for choosing raffles winners");

        var raffles = raffleRepository.findAllEndedAt(LocalDate.now().minusDays(1));
        log.info(raffles.size() + " raffles ended yesterday");
        for (var raffle : raffles) {
            var customers = orderRepository.getCustomersForEventUntil(raffle.getEvent().getId(), LocalDate.now().minusDays(1).atTime(23, 59, 59));
            if (!customers.isEmpty()) {
                int winner = random.nextInt(customers.size());
                log.info("Winner for raffle associated with event " + raffle.getEvent().getId() + " is user " + customers.get(winner));

                var user = userRepository.findById(customers.get(winner))
                        .orElseThrow(() -> new NotFoundException("User with id " + customers.get(winner) + " not found!"));

                sendEmailService.sendRaffleWinnerEmail(raffle, user, voucherService.generateVoucher(user, raffle));
            } else {
                log.info("No winner for raffle associated with event " + raffle.getEvent().getId());
            }
        }

        log.info("Ending job for choosing raffles winners");
    }
}
