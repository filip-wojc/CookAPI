package com.springtest.cookapi.infrastructure.services.review;

import com.springtest.cookapi.domain.dtos.review.CreateReviewDto;
import com.springtest.cookapi.domain.dtos.review.ReviewDto;
import com.springtest.cookapi.domain.entities.Recipe;
import com.springtest.cookapi.domain.entities.Review;
import com.springtest.cookapi.domain.entities.User;
import com.springtest.cookapi.domain.exceptions.ForbiddenException;
import com.springtest.cookapi.domain.exceptions.NotFoundException;
import com.springtest.cookapi.domain.mappers.ReviewMapper;
import com.springtest.cookapi.infrastructure.repositories.RecipeRepository;
import com.springtest.cookapi.infrastructure.repositories.ReviewRepository;
import com.springtest.cookapi.infrastructure.repositories.UserRepository;
import com.springtest.cookapi.infrastructure.services.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements IReviewService {
    private final ReviewRepository reviewRepository;
    private final RecipeRepository recipeRepository;
    private final ReviewMapper reviewMapper;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    @Override
    @CacheEvict(value = "all-reviews", allEntries = true)
    public void addReview(CreateReviewDto createReviewDto, Long recipeId) {
        var recipe = getRecipeById(recipeId);

        var currentUserId = currentUserService.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId).orElseThrow(() -> new NotFoundException("User not found with ID: " + currentUserId));

        if (recipe.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You are not allowed to review your own recipe");
        }

        if (currentUser.getReviews().stream().map(review -> review.getRecipe().getId()).collect(Collectors.toList()).contains(recipeId)) {
            throw new ForbiddenException("You are not allowed to add multiple reviews to one recipe");
        }

        var reviewToAdd = reviewMapper.toReview(createReviewDto);

        reviewToAdd.setRecipe(recipe);
        reviewToAdd.setUser(currentUser);

        reviewRepository.save(reviewToAdd);
    }

    @Override
    @Cacheable(value = "all-reviews", key = "'reviews' + #recipeId")
    public List<ReviewDto> getReviews(Long recipeId) {
        var recipe = getRecipeById(recipeId);
        List<Review> reviews = reviewRepository.getReviewByRecipe(recipe);

        return reviews.stream().map(reviewMapper::toReviewDto).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "review", key = "'review' + #reviewId")
    public ReviewDto getReviewById(Long reviewId) {
        var review = reviewRepository.findById(reviewId).orElseThrow(() -> new NotFoundException("Review not found with ID: " + reviewId));
        return reviewMapper.toReviewDto(review);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "all-reviews", allEntries = true),
            @CacheEvict(value = "review", key = "'review' + #reviewId")
    })
    public void deleteReview(Long reviewId) {
        var reviewToDelete = reviewRepository.findById(reviewId).orElseThrow(() -> new NotFoundException("Review not found with ID: " + reviewId));
        var currentUserId = currentUserService.getCurrentUserId();

        if (!currentUserId.equals(reviewToDelete.getUser().getId())) {
            throw new ForbiddenException("You are not allowed to delete this review");
        }

        reviewRepository.deleteById(reviewId);
    }

    private Recipe getRecipeById(Long recipeId) throws NotFoundException {
        Optional<Recipe> recipe = recipeRepository.findById(recipeId);
        if (!recipe.isPresent()) {
            throw new NotFoundException("Recipe with id " + recipeId + " not found");
        }
        return recipe.get();
    }
}
