package com.springtest.cookapi.domain.requests;

import com.springtest.cookapi.domain.enums.SortBy;
import com.springtest.cookapi.domain.enums.SortDirection;

public record GetRecipesRequest (
    SortBy sortBy,
    SortDirection sortDirection,
    Integer limit
){
    public GetRecipesRequest {
        if (sortBy == null) {
            sortBy = SortBy.NAME;
        }
        if (sortDirection == null) {
            sortDirection = SortDirection.ASC;
        }
        if (limit == null) {
            limit = 10;
        }
    }

    public String toString()
    {
        return sortBy.toString() + " " + sortDirection.toString() + " " + limit;
    }
}
