package com.springtest.cookapi.api.controllers;

import com.springtest.cookapi.domain.dtos.review.CreateReviewDto;
import com.springtest.cookapi.domain.dtos.review.ReviewDto;
import com.springtest.cookapi.domain.enums.SortDirection;
import com.springtest.cookapi.domain.requests.GetReviewsRequest;
import com.springtest.cookapi.domain.responses.PageResponse;
import com.springtest.cookapi.infrastructure.services.review.IReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {
    private final IReviewService reviewService;

    @PostMapping("/recipe/{recipeId}")
    public ResponseEntity<ReviewDto> addReview(
            @Valid @RequestBody CreateReviewDto createReviewDto,
            @PathVariable Long recipeId) {
        var addedReview = reviewService.addReview(createReviewDto, recipeId);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("api/review/{reviewId}")
                .buildAndExpand(addedReview.id())
                .toUri();

        return ResponseEntity.created(location).body(addedReview);
    }

    @GetMapping("/recipe/{recipeId}")
    public ResponseEntity<PageResponse<ReviewDto>> getReviews(@PathVariable Long recipeId, @RequestParam SortDirection sortDirection, @RequestParam @Min(1) @Max(50) Integer limit, @RequestParam @Min(0) Integer pageNumber) {
        return ResponseEntity.ok(reviewService.getReviews(recipeId, new GetReviewsRequest(sortDirection, limit, pageNumber)));
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
