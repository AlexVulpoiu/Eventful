package com.unibuc.fmi.eventful.services;

import com.unibuc.fmi.eventful.repository.OrderRepository;
import com.unibuc.fmi.eventful.repository.RaffleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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

    @Scheduled(cron = "0 5 0 ? * *")
    public void chooseRaffleWinner() {
        log.info("Starting job for choosing raffles winners");

        var raffles = raffleRepository.findAllEndedAt(LocalDate.now().minusDays(1));
        log.info(raffles.size() + " raffles ended yesterday");
        for (var r : raffles) {
            var customers = orderRepository.getCustomersForEventUntil(r.getEvent().getId(), LocalDate.now().minusDays(1));
            if (!customers.isEmpty()) {
                int winner = random.nextInt(customers.size());
                log.info("Winner for raffle associated with event " + r.getEvent().getId() + " is user " + customers.get(winner));
                // TODO: send email
            } else {
                log.info("No winner for raffle associated with event " + r.getEvent().getId());
            }
        }

        log.info("Ending job for choosing raffles winners");
    }
}
