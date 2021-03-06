package org.habitbev.warehousemanagement.dao;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.habitbev.warehousemanagement.entities.exceptions.ResourceNotAvailableException;
import org.habitbev.warehousemanagement.entities.sku.SKU;
import org.habitbev.warehousemanagement.entities.sku.SKUDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ConfigSKUDAOImpl implements SKUDAO {

    private static final String FINISHED_GOODS = "FINISHED-GOODS";
    private static final String WATER = "WATER";
    private static final String BAILLEY_500_ML_WATER = "BL500MLW";
    Map<String, Map<String, SKU>> companyIdToSKUMapping =
            ImmutableMap.of("VIVALA-BEVERAGES",
                    ImmutableMap.of(BAILLEY_500_ML_WATER, SKU.builder().skuCategory(FINISHED_GOODS).skuType(WATER).skuCode(BAILLEY_500_ML_WATER).build()));

    @Override
    public List<SKUDTO> getAll(String companyId) {
        Preconditions.checkArgument(StringUtils.isNotBlank(companyId), "companyId cannot be blank");
        return companyIdToSKUMapping.getOrDefault(companyId, new HashMap<>()).values().stream().map(sku -> SKUDTO.fromSKU(sku)).collect(Collectors.toList());
    }

    @Override
    public Optional<SKUDTO> get(String companyId, String skuCode) {
        Preconditions.checkArgument(StringUtils.isNotBlank(companyId), "companyId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(skuCode), "skuCode cannot be blank");

        SKU sku = companyIdToSKUMapping.get(companyId).get(skuCode);
        if (sku == null) {
            String message = String.format("Given companyId %s and skuCode %s combination does not exist", companyId, skuCode);
            throw new ResourceNotAvailableException(message);
        }
        return Optional.ofNullable(SKUDTO.fromSKU(sku));

    }
}
