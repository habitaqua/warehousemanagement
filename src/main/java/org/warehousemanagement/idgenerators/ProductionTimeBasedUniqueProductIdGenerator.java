package org.warehousemanagement.idgenerators;

import org.apache.commons.lang3.StringUtils;
import org.warehousemanagement.entities.UniqueProductIdsGenerationRequestDTO;

import java.util.ArrayList;
import java.util.List;

public class ProductionTimeBasedUniqueProductIdGenerator implements ProductIdGenerator<UniqueProductIdsGenerationRequestDTO> {

    private static final String DELIMITER = "<%>";
    private static final char PAD_CHAR = '0';
    public static final int PAD_SIZE = 4;

    @Override
    public List<String> generate(UniqueProductIdsGenerationRequestDTO input) {
        int quantity = input.getQuantity();
        List<String> generatedIds = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            String incremental_number = StringUtils.leftPad(String.valueOf(i), PAD_SIZE, PAD_CHAR);
            String id = String.join(DELIMITER, input.getCompanyId(), input.getWarehouseId(), input.getSkuCategory().getValue(),
                    input.getSkuType().getValue(),
                    String.valueOf(input.getProductionTime()), incremental_number);
            generatedIds.add(id);
        }
        return generatedIds;
    }
}
