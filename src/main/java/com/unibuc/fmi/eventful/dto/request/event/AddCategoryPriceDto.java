package com.unibuc.fmi.eventful.dto.request.event;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCategoryPriceDto {

    private long categoryId;

    @Min(1)
    private int price;
}
