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
import org.habitbev.warehousemanagement.entities.exceptions.RetriableException;
import org.habitbev.warehousemanagement.entities.inbound.FGInboundDTO;
import org.habitbev.warehousemanagement.entities.dynamodb.FinishedGoodsInbound;
import org.habitbev.warehousemanagement.entities.exceptions.NonRetriableException;
import org.habitbev.warehousemanagement.entities.exceptions.ResourceAlreadyExistsException;
import org.habitbev.warehousemanagement.entities.inbound.inboundstatus.InboundStatus;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class InboundDynamoDAOImpl implements InboundDAO {

    DynamoDBMapper inboundDynamoDbMapper;

    @Inject
    public InboundDynamoDAOImpl(DynamoDBMapper inboundDynamoDbMapper) {
        this.inboundDynamoDbMapper = inboundDynamoDbMapper;
    }

    @Override
    public Optional<FinishedGoodsInbound> get(String warehouseId, String inboundId) {
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be blank or null");
        Preconditions.checkArgument(StringUtils.isNotBlank(inboundId), "inboundId cannot be blank or null");

        try {
            FinishedGoodsInbound finishedGoodsInbound = inboundDynamoDbMapper.load(FinishedGoodsInbound.class, warehouseId, inboundId);
            return Optional.ofNullable(finishedGoodsInbound);
        } catch (InternalServerErrorException e) {
            log.error("Retriable Error occured while fetching inbound", e);
            throw new RetriableException(e);
        }
    }

    /**
     * Add the given inbound details only if there is no inbound with same id in the warehouse
     *
     * @param fgInboundDTO
     */
    public void add(FGInboundDTO fgInboundDTO) {
        Preconditions.checkArgument(fgInboundDTO != null, "fgInboundDTO cannot be null");
        Preconditions.checkArgument(fgInboundDTO.getStatus() != null, "fgInboundDTO.status cannot be null");
        Preconditions.checkArgument(fgInboundDTO.getStartTime() != null, "fgInboundDTO.startTime cannot be null");
        Preconditions.checkArgument(StringUtils.isNotBlank(fgInboundDTO.getUserId()), "fgInboundDTO.userId cannot be blank or null");

        try {

            FinishedGoodsInbound finishedGoodsInbound = fgInboundDTO.toDbEntity();
            DynamoDBSaveExpression dynamoDBSaveExpression = new DynamoDBSaveExpression();
            Map expected = new HashMap();
            expected.put("warehouseId", new ExpectedAttributeValue().withExists(false));
            expected.put("inboundId", new ExpectedAttributeValue().withExists(false));
            dynamoDBSaveExpression.withExpected(expected).withConditionalOperator(ConditionalOperator.AND);
            inboundDynamoDbMapper.save(finishedGoodsInbound, dynamoDBSaveExpression);
        } catch (InternalServerErrorException e) {
            log.error("Retriable Error occured while starting inbound", e);
            throw new RetriableException(e);
        } catch (ConditionalCheckFailedException ce) {
            log.error("Inbound", fgInboundDTO.getInboundId(), " already exist in given warehouse",
                    fgInboundDTO.getWarehouseId(), ce);
            throw new ResourceAlreadyExistsException(ce);
        } catch (Exception e) {
            log.error("Non Retriable Error occured while starting inbound", e);
            throw new NonRetriableException(e);
        }
    }

    public void update(FGInboundDTO fgInboundDTO) {
        Preconditions.checkArgument(fgInboundDTO != null, "fgInboundDTO cannot be null");

        try {
            InboundStatus newInboundStatus = fgInboundDTO.getStatus();

            DynamoDBSaveExpression dynamoDBSaveExpression = new DynamoDBSaveExpression();
            Map expected = new HashMap();
            expected.put("warehouseId", new ExpectedAttributeValue(new AttributeValue(fgInboundDTO.getWarehouseId())));
            expected.put("inboundId", new ExpectedAttributeValue(new AttributeValue(fgInboundDTO.getInboundId())));
            if (newInboundStatus != null) {
                List<AttributeValue> allowedStatuses = newInboundStatus.previousStates()
                        .stream().map(v -> new AttributeValue().withS(v.toString())).collect(Collectors.toList());
                expected.put("inboundStatus", new ExpectedAttributeValue().withComparisonOperator(ComparisonOperator.IN)
                        .withAttributeValueList(allowedStatuses));
            }

            dynamoDBSaveExpression.withExpected(expected).withConditionalOperator(ConditionalOperator.AND);
            inboundDynamoDbMapper.save(fgInboundDTO.toDbEntity(), dynamoDBSaveExpression);

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

    /**
     * gets last inbound details at warehouselevel. Used mainly to identify next Id to generate
     *
     * @param warehouseId
     * @return
     */
    public Optional<FinishedGoodsInbound> getLastInbound(String warehouseId) {
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be empty");

        try {

            Map<String, AttributeValue> eav = new HashMap();
            eav.put(":val1", new AttributeValue().withS(warehouseId));
            DynamoDBQueryExpression<FinishedGoodsInbound> dynamoDBQueryExpression = new DynamoDBQueryExpression<FinishedGoodsInbound>()
                    .withKeyConditionExpression("warehouseId = :val1").withExpressionAttributeValues(eav)
                    .withScanIndexForward(false).withLimit(1).withConsistentRead(true);
            PaginatedQueryList<FinishedGoodsInbound> queryResponse = inboundDynamoDbMapper.query(FinishedGoodsInbound.class, dynamoDBQueryExpression);
            Optional<FinishedGoodsInbound> latestInbound = queryResponse.stream().findAny();
            if (latestInbound.isPresent()) {
                return latestInbound;
            } else {
                return Optional.empty();
            }
        }catch (InternalServerErrorException e) {
            log.error("Retriable Error occured while getting last inbound", e);
            throw new RetriableException(e);
        } catch (Exception e) {
            log.error("Non Retriable Error occured while getting last inbound", e);
            throw new NonRetriableException(e);
        }
    }

}
