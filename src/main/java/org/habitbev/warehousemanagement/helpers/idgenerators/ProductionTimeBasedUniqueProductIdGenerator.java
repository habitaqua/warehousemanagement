package org.habitbev.warehousemanagement.helpers.idgenerators;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.habitbev.warehousemanagement.entities.UniqueProductIdsGenerationRequest;

import java.util.ArrayList;
import java.util.List;

public class ProductionTimeBasedUniqueProductIdGenerator implements ProductIdGenerator<UniqueProductIdsGenerationRequest> {

    private static final String DELIMITER = "<%>";
    private static final char PAD_CHAR = '0';
    private static final int PAD_SIZE = 4;

    @Override
    public List<String> generate(UniqueProductIdsGenerationRequest input) {
        Preconditions.checkArgument(input!= null, "productionTimeBasedUniqueProductIdGenerator.input cannot be null");
        int quantity = input.getQuantity();
        List<String> generatedIds = new ArrayList<>();
        for (int i = 1; i <= quantity; i++) {
            String incremental_number = StringUtils.leftPad(String.valueOf(i), PAD_SIZE, PAD_CHAR);
            String id = String.join(DELIMITER, input.getSkuCode(),
                    String.valueOf(input.getProductionTime()), incremental_number);
            generatedIds.add(id);
        }
        return generatedIds;
    }
}
