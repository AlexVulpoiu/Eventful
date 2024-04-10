package com.unibuc.fmi.eventful.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountDto {

    @NotBlank
    private String bank;

    @NotBlank
    private String iban;
}
