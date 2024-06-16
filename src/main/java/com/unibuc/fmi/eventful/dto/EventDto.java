package com.unibuc.fmi.eventful.dto;

import com.unibuc.fmi.eventful.enums.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDto {

    private Long id;

    private String name;

    private String description;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private String logo;

    private EventStatus status;

    private int charityPercentage;

    private String rejectionReason;

    private LocationDto location;

    private String organiserName;

    private int discount;

    private LocalDate discountEndDate;

    private RaffleDto raffle;

    private CharitableCauseDto charitableCause;

    private List<SeatsCategoryDetails> seatsCategories = new ArrayList<>();

    private List<StandingCategoryDto> standingCategories = new ArrayList<>();

    private List<Seat> unavailableSeats = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StandingCategoryDto {

        private String name;

        private double price;

        private double initialPrice;

        private int ticketsRemaining;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatsCategoryDetails {

        private Long id;

        private String name;

        private double price;

        private double initialPrice;

        private int minRow;

        private int maxRow;

        private int minSeat;

        private int maxSeat;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Seat {

        private int row;

        private int seat;
    }
}
