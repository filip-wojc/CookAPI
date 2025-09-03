package com.springtest.cookapi.domain.mappers;

import com.springtest.cookapi.domain.dtos.review.CreateReviewDto;
import com.springtest.cookapi.domain.dtos.review.ReviewDto;
import com.springtest.cookapi.domain.entities.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewMapper {
    private final RecipeMapper recipeMapper;
    private final UserMapper userMapper;
    public ReviewDto toReviewDto(Review review) {
        return new ReviewDto(
                review.getId(),
                review.getTitle(),
                review.getReviewContent(),
                review.getRating(),
                userMapper.toUserDto(review.getUser()),
                recipeMapper.toRecipeDto(review.getRecipe())
        );
    }

    public Review toReview(CreateReviewDto createReviewDto) {
        return new Review(
                null,
                createReviewDto.title(),
                createReviewDto.reviewContent(),
                createReviewDto.rating(),
                null,
                null
        );
    }
}
