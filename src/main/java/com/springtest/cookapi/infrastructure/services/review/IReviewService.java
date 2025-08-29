package com.springtest.cookapi.infrastructure.services.review;

import com.springtest.cookapi.domain.dtos.review.CreateReviewDto;
import com.springtest.cookapi.domain.dtos.review.ReviewDto;

import java.util.List;

public interface IReviewService {
    void addReview(CreateReviewDto createReviewDto, Long recipeId);
    List<ReviewDto> getReviews(Long recipeId);
    ReviewDto getReviewById(Long reviewId);
    void deleteReview(Long reviewId);
}
