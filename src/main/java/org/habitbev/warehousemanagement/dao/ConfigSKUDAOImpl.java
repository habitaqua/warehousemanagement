package org.habitbev.warehousemanagement.dao;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.habitbev.warehousemanagement.entities.sku.SKU;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ConfigSKUDAOImpl implements SKUDAO {

    private static final String FINISHED_GOODS = "FINISHED-GOODS";
    private static final String WATER = "WATER";
    private static final String BAILLEY_500_ML_WATER = "BAILLEY-500ML-WATER";
    Map<String, Map<String, SKU>> companyIdToSKUMapping =
            ImmutableMap.of("VIVALA-BEVERAGES",
                    ImmutableMap.of(BAILLEY_500_ML_WATER, SKU.builder().skuCategory(FINISHED_GOODS).skuType(WATER).skuCode(BAILLEY_500_ML_WATER).build()));

    @Override
    public List<SKU> getAll(String companyId) {
        Preconditions.checkArgument(StringUtils.isNotBlank(companyId), "companyId cannot be blank");
        return companyIdToSKUMapping.getOrDefault(companyId, new HashMap<>()).values().stream().collect(Collectors.toList());
    }

    @Override
    public Optional<SKU> get(String companyId, String skuCode) {
        Preconditions.checkArgument(StringUtils.isNotBlank(companyId), "companyId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(skuCode), "skuCode cannot be blank");

        SKU sku = companyIdToSKUMapping.get(companyId).get(skuCode);
       return Optional.ofNullable(sku);

    }
}
