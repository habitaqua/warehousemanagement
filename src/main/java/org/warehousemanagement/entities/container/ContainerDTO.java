package org.warehousemanagement.entities.container;

import com.google.common.base.Preconditions;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.warehousemanagement.entities.UOM;
import org.warehousemanagement.entities.container.containerstatus.Available;
import org.warehousemanagement.entities.container.containerstatus.ContainerStatus;
import org.warehousemanagement.entities.dynamodb.Container;
import org.warehousemanagement.entities.dynamodb.ContainerCapacity;
import org.warehousemanagement.utils.Utilities;

import java.util.Map;

@Data
public class ContainerDTO {

    String containerId;
    String warehouseId;
    Map<String, Integer> skuCodeWisePredefinedCapacity;
    int currentCapacity;
    ContainerStatus status;


    private ContainerDTO(String containerId, String warehouseId, Map<String, Integer> skuCodeWisePredefinedCapacity
            , Integer currentCapacity, ContainerStatus status) {

        Preconditions.checkArgument(StringUtils.isNotBlank(containerId), "id cannot be empty");
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be empty");
        Preconditions.checkArgument(Utilities.validateContainerPredefinedCapacities(skuCodeWisePredefinedCapacity),
                "total capacity cannot be zero or empty");
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be empty");
        Preconditions.checkArgument(currentCapacity >= 0, "current capacity not in range");
        Preconditions.checkArgument(status != null, "location status cannot be null");

        this.containerId = containerId;
        this.warehouseId = warehouseId;
        this.skuCodeWisePredefinedCapacity = skuCodeWisePredefinedCapacity;
        this.currentCapacity = currentCapacity;
        this.status = status;
    }

    public static class Builder {

        String containerId;
        String warehouseId;
        Map<String, Integer> skuCodeWisePredefinedCapacity;
        int currentCapacity;
        UOM uom;
        ContainerStatus status;

        public Builder containerId(String containerId) {
            this.containerId = containerId;
            return this;
        }

        public Builder warehouseId(String warehouseId) {
            this.warehouseId = warehouseId;
            return this;
        }

        public Builder predefinedCapacity(Map<String, Integer> skuCodeWisePredefinedCapacity) {
            this.skuCodeWisePredefinedCapacity = skuCodeWisePredefinedCapacity;
            return this;
        }

        public Builder containerDetails(Container container) {
            this.containerId = container.getContainerId();
            this.warehouseId = container.getWarehouseId();
            this.skuCodeWisePredefinedCapacity = container.getSkuCodeWisePredefinedCapacity();
            return this;
        }

        public Builder currentCapacityDetails(ContainerCapacity containerCapacity) {
            this.currentCapacity = containerCapacity.getCurrentCapacity();
            this.status = containerCapacity.getStatus();
            return this;
        }

        public ContainerDTO build() {
            if (this.status == null) {
                this.status = new Available();
            }
            return new ContainerDTO(this);
        }
    }

    private ContainerDTO(Builder b) {
        this.containerId = b.containerId;
        this.warehouseId = b.warehouseId;
        this.skuCodeWisePredefinedCapacity = b.skuCodeWisePredefinedCapacity;
        this.currentCapacity = b.currentCapacity;
        this.status = b.status;
    }
}
