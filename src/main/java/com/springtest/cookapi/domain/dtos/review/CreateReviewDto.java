package com.springtest.cookapi.domain.dtos.review;

import jakarta.validation.constraints.*;

public record CreateReviewDto (
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title cannot exceed 100 characters")
    String title,

    @NotBlank(message = "Review content is required")
    @Size(max = 2000, message = "Review content cannot exceed 2000 characters")
    String reviewContent,

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 10, message = "Rating cannot exceed 10")
    Integer rating
){}
