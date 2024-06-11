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
public class ProfileDto {

    private String name;

    private String email;

    private String phone;

    private int xp;

    private int availablePoints;

    private List<OrderDetailsDto> orders;
}
