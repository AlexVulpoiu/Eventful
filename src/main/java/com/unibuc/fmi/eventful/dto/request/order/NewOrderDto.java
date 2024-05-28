package com.unibuc.fmi.eventful.dto.request.order;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewOrderDto {

    private long eventId;

    @Min(0)
    private int discountPoints;

    private Map<String, Integer> standingTickets;

    private List<SeatDetails> seatedTicketsDetails;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatDetails {

        private long categoryId;

        private int row;

        private int seat;
    }
}
