package org.habitbev.warehousemanagement.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.*;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.habitbev.warehousemanagement.entities.dynamodb.FinishedGoodsOutbound;
import org.habitbev.warehousemanagement.entities.outbound.OutboundDTO;
import org.habitbev.warehousemanagement.entities.outbound.outboundstatus.OutboundStatus;
import org.habitbev.warehousemanagement.entities.exceptions.NonRetriableException;
import org.habitbev.warehousemanagement.entities.exceptions.ResourceAlreadyExistsException;
import org.habitbev.warehousemanagement.entities.exceptions.RetriableException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class OutboundDynamoDAOImpl implements OutboundDAO {

    DynamoDBMapper outboundDynamoDbMapper;


    @Inject
    public OutboundDynamoDAOImpl(DynamoDBMapper outboundDynamoDbMapper) {
        this.outboundDynamoDbMapper = outboundDynamoDbMapper;
    }

    public void add(OutboundDTO outboundDTO) {
        try {
            Preconditions.checkArgument(outboundDTO != null, "outboundDTO cannot be null");
            Preconditions.checkArgument(outboundDTO.getStatus() != null, "outboundDTO.status cannot be null");
            Preconditions.checkArgument(outboundDTO.getStartTime() != null, "outboundDTO.startTime cannot be null");
            Preconditions.checkArgument(StringUtils.isNotBlank(outboundDTO.getUserId()), "outboundDTO.userId cannot be blank or null");
            Preconditions.checkArgument(StringUtils.isNotBlank(outboundDTO.getCustomerId()), "outboundDTO.customerId cannot be blank or null");


            FinishedGoodsOutbound finishedGoodsOutbound = outboundDTO.toDbEntity();
            DynamoDBSaveExpression dynamoDBSaveExpression = new DynamoDBSaveExpression();
            Map expected = new HashMap();
            expected.put("warehouseId", new ExpectedAttributeValue().withExists(false));
            expected.put("outboundId", new ExpectedAttributeValue().withExists(false));
            dynamoDBSaveExpression.withExpected(expected).withConditionalOperator(ConditionalOperator.AND);
            outboundDynamoDbMapper.save(finishedGoodsOutbound, dynamoDBSaveExpression);
        } catch (InternalServerErrorException e) {
            log.error("Retriable Error occurred while starting inbound", e);
            throw new RetriableException(e);
        } catch (ConditionalCheckFailedException ce) {
            log.error("Outbound", outboundDTO.getOutboundId(), " already exist in given warehouse",
                    outboundDTO.getWarehouseId(), ce);
            throw new ResourceAlreadyExistsException(ce);
        } catch (Exception e) {
            log.error("Non Retriable Error occured while starting inbound", e);
            throw new NonRetriableException(e);
        }
    }

    public void update(OutboundDTO outboundDTO) {
        try {
            Preconditions.checkArgument(outboundDTO != null, "fgInboundDTO cannot be null");
            OutboundStatus newOutboundStatus = outboundDTO.getStatus();

            DynamoDBSaveExpression dynamoDBSaveExpression = new DynamoDBSaveExpression();
            Map expected = new HashMap();
            expected.put("warehouseId", new ExpectedAttributeValue(new AttributeValue(outboundDTO.getWarehouseId())));
            expected.put("outboundId", new ExpectedAttributeValue(new AttributeValue(outboundDTO.getOutboundId())));

            if (newOutboundStatus != null) {
                List<AttributeValue> allowedStatuses = newOutboundStatus.previousStates()
                        .stream().map(v -> new AttributeValue().withS(v.getStatus())).collect(Collectors.toList());
                expected.put("outboundStatus", new ExpectedAttributeValue().withComparisonOperator(ComparisonOperator.IN)
                        .withAttributeValueList(allowedStatuses));
            }
            dynamoDBSaveExpression.withExpected(expected).withConditionalOperator(ConditionalOperator.AND);
            outboundDynamoDbMapper.save(outboundDTO.toDbEntity(), dynamoDBSaveExpression);

        } catch (InternalServerErrorException ie) {
            log.error("Retriable Error occured while updating inbound", ie);
            throw new RetriableException(ie);
        } catch (ConditionalCheckFailedException ce) {
            log.error("conditional check occured while updating inbound", ce.getCause());
            throw new NonRetriableException(ce);
        } catch (Exception e) {
            log.error("Non Retriable Error occured while updating inbound", e);
            throw new NonRetriableException(e);
        }
    }

    public Optional<FinishedGoodsOutbound> getLastOutbound(String warehouseId) {
        try {
            Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be empty");
            Map<String, AttributeValue> eav = new HashMap();
            eav.put(":val1", new AttributeValue().withS(warehouseId));
            DynamoDBQueryExpression<FinishedGoodsOutbound> dynamoDBQueryExpression = new DynamoDBQueryExpression<FinishedGoodsOutbound>()
                    .withKeyConditionExpression("warehouseId = :val1").withExpressionAttributeValues(eav)
                    .withScanIndexForward(false).withLimit(1).withConsistentRead(true);
            PaginatedQueryList<FinishedGoodsOutbound> queryResponse = outboundDynamoDbMapper.query(FinishedGoodsOutbound.class, dynamoDBQueryExpression);
            Optional<FinishedGoodsOutbound> latestOutbound = queryResponse.stream().findAny();
            if (latestOutbound.isPresent()) {
                return latestOutbound;
            } else {
                return Optional.empty();
            }
        } catch (InternalServerErrorException e) {
            log.error("Retriable Error occured while getting last inbound", e);
            throw new RetriableException(e);
        } catch (Exception e) {
            log.error("Non Retriable Error occured while getting last inbound", e);
            throw new NonRetriableException(e);
        }
    }
}
