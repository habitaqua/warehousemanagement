package org.warehousemanagement.entities;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Optional;

@Value
@Builder
public class PaginatedResponse<T> {

    List<T> items;
    String nextPageToken;


    public String getNextPageToken() {
        return nextPageToken;
    }
}
