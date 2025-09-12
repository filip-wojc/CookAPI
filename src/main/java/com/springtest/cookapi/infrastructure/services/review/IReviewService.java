package com.springtest.cookapi.infrastructure.services.review;

import com.springtest.cookapi.domain.dtos.review.CreateReviewDto;
import com.springtest.cookapi.domain.dtos.review.ReviewDto;
import com.springtest.cookapi.domain.requests.GetReviewsRequest;
import com.springtest.cookapi.domain.responses.PageResponse;

public interface IReviewService {
    ReviewDto addReview(CreateReviewDto createReviewDto, Long recipeId);
    PageResponse<ReviewDto> getReviews(Long recipeId, GetReviewsRequest getReviewsRequest);
    ReviewDto getReviewById(Long reviewId);
    void deleteReview(Long reviewId);
}
