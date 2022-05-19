package org.warehousemanagement.entities.location;

import lombok.Builder;
import lombok.Value;

import java.util.Optional;

@Value
public class GetLocationsRequest {

    String warehouseId;
    String pageToken;
    int limit;
    boolean isFirst;

    /**
     *
     * @param warehouseId warehouseId
     * @param pageToken next page token
     * @param limit
     * @param isFirst page token is null for both last and first request. This flag is used to differentiate.
     */
    @Builder
    public GetLocationsRequest(String warehouseId, String pageToken, int limit, boolean isFirst) {
        this.warehouseId = warehouseId;
        this.pageToken = pageToken;
        this.limit = limit;
        this.isFirst = isFirst;
    }

    public Optional<String> getPageToken() {
        return Optional.ofNullable(pageToken);
    }
}
