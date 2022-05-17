package org.warehousenamagement.entities.location;

import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.warehousenamagement.entities.Capacity;
import org.warehousenamagement.entities.SKUCategory;
import org.warehousenamagement.entities.SKUType;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Value
public class LocationDTO {

    String id;
    String warehouseId;
    Set<SKUType> supportedSKUTypes;
    Set<SKUCategory> supportedSKUCategories;
    boolean allSKUSupported;
    Map<SKUType, Capacity> currentSKUView;
    boolean isActive;

    @Builder
    private LocationDTO(String id, String warehouseId, Set<SKUType> supportedSKUTypes,
            Set<SKUCategory> supportedSKUCategories, Boolean allSKUSupported,
            Map<SKUType, Capacity> currentSKUView, Boolean isActive) {

        Preconditions.checkArgument(id != null, "id cannot be null");
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be empty");
        this.supportedSKUTypes = supportedSKUTypes == null ? new HashSet<>() : supportedSKUTypes;
        this.supportedSKUCategories = supportedSKUCategories == null ? new HashSet<>() : supportedSKUCategories;
        this.id = id;
        this.warehouseId = warehouseId;
        this.allSKUSupported = allSKUSupported;

        if ((!allSKUSupported || null == allSKUSupported) && supportedSKUTypes.isEmpty() && supportedSKUCategories
                .isEmpty()) {
            throw new UnsupportedOperationException("List supportedSKU Types/ Categories or allow all SKU Types");
        }
        this.isActive = isActive;
        if (!isSKUViewValid()) {
            throw new UnsupportedOperationException("sku type unsupported at the location");
        }
        this.currentSKUView = currentSKUView;
    }

    private boolean isSKUViewValid() {
        if (!currentSKUView.isEmpty() && !allSKUSupported) {

            Set<SKUType> currentSKUTypes = currentSKUView.keySet();
            Set<SKUType> finalSupportedSKUTypes = new HashSet<>();

            this.supportedSKUCategories.stream()
                    .forEach(skuCategory -> finalSupportedSKUTypes.addAll(skuCategory.getSupportedSKUs()));
            this.supportedSKUTypes.stream().forEach(skuType -> finalSupportedSKUTypes.add(skuType));
            return finalSupportedSKUTypes.containsAll(currentSKUTypes);

        }
        return true;
    }
}
