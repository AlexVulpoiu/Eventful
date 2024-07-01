package com.unibuc.fmi.eventful.services;

import com.unibuc.fmi.eventful.model.Order;
import com.unibuc.fmi.eventful.model.Raffle;
import com.unibuc.fmi.eventful.model.User;
import com.unibuc.fmi.eventful.model.Voucher;
import com.unibuc.fmi.eventful.repository.RaffleRepository;
import com.unibuc.fmi.eventful.repository.VoucherRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VoucherService {

    RaffleRepository raffleRepository;
    VoucherRepository voucherRepository;

    public Voucher generateVoucher(Order order) {
        var raffle = order.getEvent().getRaffle();
        if (raffle == null || raffle.getTotalParticipants() >= raffle.getParticipantsLimit()) {
            return null;
        }

        log.info("Generating voucher for order " + order.getId());
        raffle.increaseTotalParticipants();
        raffleRepository.save(raffle);

        var voucher = Voucher.builder()
                .name(raffle.getPartnerName())
                .value(raffle.getPrize())
                .code(RandomStringUtils.random(8, 0, 0, true, true, null, new SecureRandom()))
                .order(order)
                .build();

        return voucherRepository.save(voucher);
    }

    public Voucher generateVoucher(User user, Raffle raffle) {
        log.info("Generating voucher for user " + user.getId() + " and raffle " + raffle.getId());
        raffle.setUser(user);
        raffleRepository.save(raffle);

        var voucher = Voucher.builder()
                .name(raffle.getPartnerName())
                .value(raffle.getPrize())
                .code(RandomStringUtils.random(8, 0, 0, true, true, null, new SecureRandom()))
                .raffle(raffle)
                .build();

        return voucherRepository.save(voucher);
    }
}
