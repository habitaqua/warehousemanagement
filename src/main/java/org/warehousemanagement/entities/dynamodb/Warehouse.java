package org.warehousemanagement.entities.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "warehouse")
public class Warehouse {

    String id;
    String companyId;
    String name;
    String address;
    String details;
}
