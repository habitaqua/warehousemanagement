package org.warehousemanagement.entities.container;

import lombok.Builder;
import lombok.Value;

import java.util.Optional;

@Value
public class GetContainersRequest {

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
    public GetContainersRequest(String warehouseId, String pageToken, int limit, boolean isFirst) {
        this.warehouseId = warehouseId;
        this.pageToken = pageToken;
        this.limit = limit;
        this.isFirst = isFirst;
    }

    public Optional<String> getPageToken() {
        return Optional.ofNullable(pageToken);
    }
}
