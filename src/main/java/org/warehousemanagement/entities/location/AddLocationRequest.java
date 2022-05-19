package org.warehousemanagement.entities.location;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.warehousemanagement.entities.Capacity;

@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
public class AddLocationRequest {
    @JsonProperty("warehouseId")
    String warehouseId;
    @JsonProperty("companyId")
    String companyId;

    Capacity totalCapacity;



    @Builder
    public AddLocationRequest(String warehouseId, String companyId, Capacity totalCapacity) {
        this.warehouseId = warehouseId;
        this.companyId = companyId;
        this.totalCapacity = totalCapacity;

    }
}
