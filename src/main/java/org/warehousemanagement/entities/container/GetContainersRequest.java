package org.warehousemanagement.entities.container;

import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

@Value
public class GetContainersRequest {

    String warehouseId;
    String pageToken;
    int limit;

    /**
     *
     * @param warehouseId warehouseId
     * @param pageToken next page token
     * @param limit
     */
    @Builder
    public GetContainersRequest(String warehouseId, String pageToken, int limit) {
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be blank");
        this.warehouseId = warehouseId;
        this.pageToken = pageToken;
        this.limit = limit;
    }

    public Optional<String> getPageToken() {
        return Optional.ofNullable(pageToken);
    }
}
