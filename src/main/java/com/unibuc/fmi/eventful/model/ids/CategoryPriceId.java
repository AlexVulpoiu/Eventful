package com.unibuc.fmi.eventful.model.ids;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class CategoryPriceId implements Serializable {

    private Long categoryId;

    private Long eventId;
}
