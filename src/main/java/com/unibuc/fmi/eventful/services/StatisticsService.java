package com.unibuc.fmi.eventful.services;

import com.unibuc.fmi.eventful.dto.OrganiserStatisticsDto;
import com.unibuc.fmi.eventful.exceptions.NotFoundException;
import com.unibuc.fmi.eventful.repository.EventRepository;
import com.unibuc.fmi.eventful.repository.OrderRepository;
import com.unibuc.fmi.eventful.repository.OrganiserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StatisticsService {

    EventRepository eventRepository;
    OrderRepository orderRepository;
    OrganiserRepository organiserRepository;

    public OrganiserStatisticsDto getStatistics(Long organiserId) {
        organiserRepository.findById(organiserId)
                .orElseThrow(() -> new NotFoundException("Organiser with id " + organiserId + " not found!"));

        LocalDate currentDate = LocalDate.now();
        LocalDate startDate = currentDate.minusMonths(5);
        List<String> months = new ArrayList<>();
        List<Double> monthlyIncome = new ArrayList<>();
        List<Integer> eventsPerMonth = new ArrayList<>();

        for (LocalDate iterator = startDate; !iterator.isAfter(currentDate); iterator = iterator.plusMonths(1)) {
            months.add(String.valueOf(iterator.getMonth()));
            var events = eventRepository.getEventsForOrganiserHavingStartingMonth(organiserId, iterator.getMonthValue(), iterator.getYear());
            eventsPerMonth.add(events.size());

            double income = 0.0;
            for (var event : events) {
                Double eventIncome = orderRepository.getIncomeForEvent(event.getId());
                income += (eventIncome == null ? 0.0 : eventIncome);
            }
            monthlyIncome.add(income);
        }

        var charitableEventsThisYear = eventRepository.getCharitableEventsByOrganiserAndYear(organiserId, currentDate.getYear());
        var charitableEventsLastYear = eventRepository.getCharitableEventsByOrganiserAndYear(organiserId, currentDate.getYear() - 1);
        var charityIncome = 0.0;
        for (var event : charitableEventsThisYear) {
            charityIncome += event.getCharityPercentage() * orderRepository.getIncomeForEvent(event.getId()) / 100;
        }
        var charityIncrease = 100.0;
        if (!charitableEventsLastYear.isEmpty()) {
            charityIncrease = (1.0 * (charitableEventsThisYear.size() - charitableEventsLastYear.size()) / charitableEventsLastYear.size()) * 100.0;
            charityIncrease = Double.parseDouble(String.format("%.2f%%", charityIncrease));
        }

        return OrganiserStatisticsDto.builder()
                .months(months)
                .incomePerMonth(monthlyIncome)
                .eventsPerMonth(eventsPerMonth)
                .charityAmount(charityIncome)
                .causesThisYear(charitableEventsThisYear.size())
                .causesLastYear(charitableEventsLastYear.size())
                .charityIncrease(charityIncrease)
                .build();
    }
}
