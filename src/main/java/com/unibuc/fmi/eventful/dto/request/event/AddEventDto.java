package com.unibuc.fmi.eventful.dto.request.event;

import com.unibuc.fmi.eventful.dto.request.charitablecause.AddCharitableCauseDto;
import com.unibuc.fmi.eventful.enums.FeeSupporter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddEventDto {

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @Future
    private LocalDateTime startDate;

    @Future
    private LocalDateTime endDate;

    @Min(1)
    private int preparationTime;

    @NotNull
    private FeeSupporter feeSupporter;

    private long locationId;

    // TODO: tombole si promotii
    @Min(0)
    private int charityPercentage;

    private AddCharitableCauseDto addCharitableCause;

    private List<AddCategoryPriceDto> categoriesPrices;

    private List<@Valid AddStandingCategoryDto> standingCategories;

    public LocalDateTime getStartDateWithPreparationTime() {
        return startDate.minusHours(preparationTime);
    }

    public LocalDateTime getEndDateWithPreparationTime() {
        return endDate.plusHours(preparationTime);
    }
}
