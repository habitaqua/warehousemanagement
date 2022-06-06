package org.habitbev.warehousemanagement.helpers.idgenerators;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ListAssert;
import org.junit.Before;
import org.junit.Test;
import org.habitbev.warehousemanagement.entities.UniqueProductIdsGenerationRequest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TestProductionTimeBasedUniqueProductIdGenerator {
    private static final String DELIMITER = "<%>";
    private static final char PAD_CHAR = '0';
    private static final int PAD_SIZE = 4;
    public static final String COMPANY_1 = "company-1";
    public static final String SKU_CATEGORY = "sku-category";
    public static final String SKU_TYPE = "sku-type";
    public static final String SKU_CODE = "sku-code";
    public static final String WAREHOUSE_ID = "warehouse-id";
    ProductionTimeBasedUniqueProductIdGenerator productionTimeBasedUniqueProductIdGenerator;

    @Before
    public void setUp() throws Exception {
        productionTimeBasedUniqueProductIdGenerator = new ProductionTimeBasedUniqueProductIdGenerator();
    }

    @Test
    public void test_generate_input_null_illegal_argument_exception() {

        Assertions.assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> productionTimeBasedUniqueProductIdGenerator.generate(null));
    }

    @Test
    public void test_generate_success() {

        long productionTime = Instant.now().toEpochMilli();
        UniqueProductIdsGenerationRequest uniqueProductIdsGenerationRequest = UniqueProductIdsGenerationRequest.builder().productionTime(productionTime).companyId(COMPANY_1).quantity(1000).skuCategory(SKU_CATEGORY)
                .skuType(SKU_TYPE).skuCode(SKU_CODE).warehouseId(WAREHOUSE_ID).build();
        List<String> expectedIds = getExpectedIds(uniqueProductIdsGenerationRequest);
        List<String> actualIds = productionTimeBasedUniqueProductIdGenerator.generate(uniqueProductIdsGenerationRequest);
        new ListAssert<>(expectedIds).containsExactlyInAnyOrderElementsOf(actualIds);
    }


    private List<String> getExpectedIds(UniqueProductIdsGenerationRequest input) {
        int quantity = input.getQuantity();
        List<String> generatedIds = new ArrayList<>();
        for (int i = 1; i <= quantity; i++) {
            String incremental_number = StringUtils.leftPad(String.valueOf(i), PAD_SIZE, PAD_CHAR);
            String id = String.join(DELIMITER, input.getCompanyId(), input.getWarehouseId(), input.getSkuCategory(),
                    input.getSkuType(), String.valueOf(input.getProductionTime()), incremental_number);
            generatedIds.add(id);
        }
        return generatedIds;

    }
}

