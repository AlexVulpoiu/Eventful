package com.unibuc.fmi.eventful.model;

import com.unibuc.fmi.eventful.enums.EventStatus;
import com.unibuc.fmi.eventful.enums.FeeSupporter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    private int charityPercentage = 0;

    private String rejectionReason;

    private double rating = 0.0;

    private LocalDateTime updatedAt;

    @ManyToOne
    private AbstractLocation location;

    @OneToOne
    private CharitableCause charitableCause;

    @ManyToOne
    private Organiser organiser;

    @OneToMany(mappedBy = "event", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<CategoryPrice> categoryPrices;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<StandingCategory> standingCategories;

    @OneToMany(mappedBy = "event", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Review> reviews = new ArrayList<>();

    public LocalDateTime getStartDateWithPreparationTime() {
        return startDate.minusHours(preparationTime);
    }

    public LocalDateTime getEndDateWithPreparationTime() {
        return endDate.plusHours(preparationTime);
    }

    public void addReview(Review review) {
        this.reviews.add(review);
        this.reviews.stream().mapToDouble(Review::getRating).average()
                .ifPresentOrElse(d -> this.rating = Math.floor(d * 100) / 100, () -> this.rating = 0.0);
    }
}
