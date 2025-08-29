package com.springtest.cookapi.api.controllers;

import com.springtest.cookapi.domain.dtos.review.CreateReviewDto;
import com.springtest.cookapi.domain.dtos.review.ReviewDto;
import com.springtest.cookapi.infrastructure.services.review.IReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {
    private final IReviewService reviewService;

    @PostMapping("/recipe/{recipeId}")
    public ResponseEntity<String> addReview(
            @Valid @RequestBody CreateReviewDto createReviewDto,
            @PathVariable Long recipeId) {
        reviewService.addReview(createReviewDto, recipeId);
        return ResponseEntity.ok("Review added successfully");
    }

    @GetMapping("/recipe/{recipeId}")
    public ResponseEntity<List<ReviewDto>> getReviews(@PathVariable Long recipeId) {
        return ResponseEntity.ok(reviewService.getReviews(recipeId));
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> getReviewById(@PathVariable Long reviewId) {
        return ResponseEntity.ok(reviewService.getReviewById(reviewId));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}
