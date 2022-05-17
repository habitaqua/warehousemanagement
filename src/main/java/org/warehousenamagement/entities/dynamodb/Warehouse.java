package org.warehousenamagement.entities.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "warehouseDetails")
public class Warehouse {

    String id;
    String name;
    String address;
    String details;
}
