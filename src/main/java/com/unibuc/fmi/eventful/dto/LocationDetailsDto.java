package com.unibuc.fmi.eventful.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationDetailsDto {

    private long id;

    private String name;

    private List<String> categories;

    private List<Long> categoriesIds;

    private int capacity;
}
