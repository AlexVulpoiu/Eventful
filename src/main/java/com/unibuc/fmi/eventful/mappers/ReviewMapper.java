package com.unibuc.fmi.eventful.mappers;

import com.unibuc.fmi.eventful.dto.ReviewDto;
import com.unibuc.fmi.eventful.model.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "userId", source = "review.user.id")
    ReviewDto reviewToReviewDto(Review review);
}
