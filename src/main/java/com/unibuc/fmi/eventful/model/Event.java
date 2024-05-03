package com.unibuc.fmi.eventful.model;

import com.unibuc.fmi.eventful.enums.EventStatus;
import com.unibuc.fmi.eventful.enums.FeeSupporter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private int preparationTime;

    private String logo;

    @Enumerated(value = EnumType.STRING)
    private EventStatus status;

    @Enumerated(value = EnumType.STRING)
    private FeeSupporter feeSupporter;

    private int charityPercentage;

    private String rejectionReason;

    @ManyToOne
    private AbstractLocation location;

    @ManyToOne
    private CharitableCause charitableCause;

    @ManyToOne
    private Organiser organiser;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<CategoryPrice> categoryPrices;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<StandingCategory> standingCategories;

    public LocalDateTime getStartDateWithPreparationTime() {
        return startDate.minusMinutes(preparationTime);
    }

    public LocalDateTime getEndDateWithPreparationTime() {
        return endDate.plusMinutes(preparationTime);
    }
}
