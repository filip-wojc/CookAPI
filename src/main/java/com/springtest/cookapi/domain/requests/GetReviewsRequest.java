package com.springtest.cookapi.domain.requests;

import com.springtest.cookapi.domain.enums.SortDirection;

import java.io.Serializable;

public record GetReviewsRequest (
        SortDirection sortDirection,
        Integer pageNumber,
        Integer limit
) implements Serializable {
    public GetReviewsRequest {
        if (sortDirection == null) {
            sortDirection = SortDirection.ASC;
        }
        if (pageNumber == null) {
            pageNumber = 0;
        }
        if (limit == null) {
            limit = 10;
        }
    }
}