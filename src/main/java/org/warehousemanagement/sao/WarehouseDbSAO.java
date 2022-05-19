package org.warehousemanagement.sao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.google.inject.Inject;
import org.warehousemanagement.entities.dynamodb.Warehouse;

import java.util.Optional;

public class WarehouseDbSAO {

    DynamoDBMapper warehouseDynamoDbMapper;

    @Inject
    public WarehouseDbSAO(DynamoDBMapper warehouseDynamoDbMapper) {
        this.warehouseDynamoDbMapper = warehouseDynamoDbMapper;
    }

    Optional<Warehouse> getWarehouse(String warehouseId, String companyId) {

        Warehouse warehouse = warehouseDynamoDbMapper.load(Warehouse.class, warehouseId, companyId);
        return Optional.ofNullable(warehouse);
    }
}
