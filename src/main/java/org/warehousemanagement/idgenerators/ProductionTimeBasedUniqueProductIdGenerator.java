package org.warehousemanagement.idgenerators;

import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.warehousemanagement.entities.SKUBarcodesGenerationRequestDTO;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

public class ProductionTimeBasedUniqueProductIdGenerator implements ProductIdGenerator<SKUBarcodesGenerationRequestDTO> {

    private static final String DELIMITER = "<%>";
    private static final char PAD_CHAR = '0';
    public static final int PAD_SIZE = 4;
    Clock clock;

    @Inject
    public ProductionTimeBasedUniqueProductIdGenerator(Clock clock) {
        this.clock = clock;
    }

    @Override
    public List<String> generate(SKUBarcodesGenerationRequestDTO input) {
        long productionTime = clock.millis();
        int quantity = input.getQuantity();
        List<String> generatedIds = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            String incremental_number = StringUtils.leftPad(String.valueOf(i), PAD_SIZE, PAD_CHAR);
            String id = String.join(DELIMITER, input.getSkuCategory().getValue(),
                    input.getCompanyId(), input.getWarehouseId(), input.getSkuType().getValue(), String.valueOf(productionTime), incremental_number);
            generatedIds.add(id);
        }
        return generatedIds;
    }
}
