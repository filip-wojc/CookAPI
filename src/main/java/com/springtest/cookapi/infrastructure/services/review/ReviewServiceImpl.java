package com.springtest.cookapi.infrastructure.services.review;

import com.springtest.cookapi.domain.dtos.review.CreateReviewDto;
import com.springtest.cookapi.domain.dtos.review.ReviewDto;
import com.springtest.cookapi.domain.entities.Recipe;
import com.springtest.cookapi.domain.entities.User;
import com.springtest.cookapi.domain.enums.SortDirection;
import com.springtest.cookapi.domain.exceptions.ForbiddenException;
import com.springtest.cookapi.domain.exceptions.NotFoundException;
import com.springtest.cookapi.domain.mappers.ReviewMapper;
import com.springtest.cookapi.domain.requests.GetReviewsRequest;
import com.springtest.cookapi.domain.responses.PageResponse;
import com.springtest.cookapi.infrastructure.repositories.RecipeRepository;
import com.springtest.cookapi.infrastructure.repositories.ReviewRepository;
import com.springtest.cookapi.infrastructure.repositories.UserRepository;
import com.springtest.cookapi.infrastructure.services.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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
    public ReviewDto addReview(CreateReviewDto createReviewDto, Long recipeId) {
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

        var addedReview = reviewRepository.save(reviewToAdd);

        setRecipeRating(recipe);

        return reviewMapper.toReviewDto(addedReview);
    }

    @Override
    @Cacheable(value = "all-reviews", key = "'reviews' + #recipeId + ' ' + #getReviewsRequest.toString()")
    public PageResponse<ReviewDto> getReviews(Long recipeId, GetReviewsRequest getReviewsRequest) {
        var recipe = getRecipeById(recipeId);

        Sort.Direction sortDirection = getReviewsRequest.sortDirection() == SortDirection.ASC ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortBy = "rating";

        PageRequest pageRequest = PageRequest.of(
                getReviewsRequest.limit(),
                getReviewsRequest.pageNumber(),
                Sort.by(sortDirection, sortBy)
        );

        var reviews = reviewRepository.findByRecipe(recipe, pageRequest);
        return PageResponse.of(reviews.map(reviewMapper::toReviewDto));
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

        var recipe = reviewToDelete.getRecipe();
        reviewRepository.deleteById(reviewId);
        setRecipeRating(recipe);
    }

    private Recipe getRecipeById(Long recipeId) throws NotFoundException {
        Optional<Recipe> recipe = recipeRepository.findById(recipeId);
        if (!recipe.isPresent()) {
            throw new NotFoundException("Recipe with id " + recipeId + " not found");
        }
        return recipe.get();
    }

    private void setRecipeRating(Recipe recipe) {
        var allReviews = reviewRepository.findByRecipe(recipe);
        if (allReviews.isEmpty()) {
            recipe.setRating(null);
        }
        else {
            Double rating = allReviews.stream().mapToDouble(review -> review.getRating()).average().orElse(0.0);
            recipe.setRating(rating);
        }
        recipeRepository.save(recipe);
    }
}
