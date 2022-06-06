package org.habitbev.warehousemanagement.entities.dynamodb.typeconvertors;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.habitbev.warehousemanagement.entities.exceptions.NonRetriableException;

import java.util.Map;

public class SKUWiseCapacityConvertor implements DynamoDBTypeConverter<String, Map<String, Integer>> {
    ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public String convert(Map<String, Integer> input) {

        try {
            return objectMapper.writeValueAsString(input);
        } catch (JsonProcessingException e) {
            throw new NonRetriableException(e);
        }
    }

    @Override
    public Map<String, Integer> unconvert(String input) {
        TypeReference<Map<String,Integer>> typeRef
                = new TypeReference<Map<String,Integer>>() {};

        try {
            return objectMapper.readValue(input, typeRef);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
