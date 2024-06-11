package com.unibuc.fmi.eventful.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private int charityPercentage;

    private String rejectionReason;

    private LocationDto location;

    // TODO: add charitable cause

    private String organiserName;

    private double organiserRating;

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
