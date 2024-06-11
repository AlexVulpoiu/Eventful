package com.unibuc.fmi.eventful.dto;

import com.unibuc.fmi.eventful.model.AbstractLocation;
import com.unibuc.fmi.eventful.model.SeatedLocation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationDto {

    private long id;

    private String fullAddress;

    private int rows;

    private int seatsPerRow;

    public static LocationDto from(AbstractLocation abstractLocation) {
        var location = LocationDto.builder()
                .id(abstractLocation.getId())
                .fullAddress(abstractLocation.getFullAddressWithName())
                .rows(0)
                .seatsPerRow(0)
                .build();
        if (abstractLocation instanceof SeatedLocation seatedLocation) {
            location.setRows(seatedLocation.getNumberOfRows());
            location.setSeatsPerRow(seatedLocation.getSeatsPerRow());
        }
        return location;
    }
}
