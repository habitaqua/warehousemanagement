package org.habitbev.warehousemanagement.dao.inmemoryDb;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.model.*;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.BooleanAssert;
import org.assertj.core.api.LongAssert;
import org.assertj.core.api.StringAssert;
import org.habitbev.warehousemanagement.dao.OutboundDynamoDAOImpl;
import org.habitbev.warehousemanagement.entities.dynamodb.FinishedGoodsOutbound;
import org.habitbev.warehousemanagement.entities.exceptions.NonRetriableException;
import org.habitbev.warehousemanagement.entities.exceptions.ResourceAlreadyExistsException;
import org.habitbev.warehousemanagement.entities.outbound.OutboundDTO;
import org.habitbev.warehousemanagement.entities.outbound.outboundstatus.Active;
import org.habitbev.warehousemanagement.entities.outbound.outboundstatus.Closed;
import org.habitbev.warehousemanagement.testutils.LocalDbCreationRule;
import org.habitbev.warehousemanagement.testutils.Utilities;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.time.Clock;
import java.util.Optional;
import java.util.Properties;


public class TestInmemoryDbOutboundDynamoDAOImpl {


    private static final String OUTBOUND_1 = "OUTBOUND-1";
    private static final String OUTBOUND_2 = "OUTBOUND-2";

    private static final String WAREHOUSE_1 = "WAREHOUSE-1";
    private static final String WAREHOUSE_2 = "WAREHOUSE-2";

    private static final String USER_ID = "user-1";
    private static final String CUSTOMER_ID = "customer-1";

    @ClassRule
    public static LocalDbCreationRule dynamoDB = new LocalDbCreationRule();
    OutboundDynamoDAOImpl outboundDynamoDAO;
    DynamoDBMapper dynamoDBMapper;
    AmazonDynamoDB amazonDynamoDB;

    Clock clock;

    DynamoDBMapperConfig dynamoDBMapperConfig;


    @Before
    public void setup() {
        Properties testProperties = Utilities.getTestProperties();
        String amazonAWSAccessKey = testProperties.getProperty(Utilities.AWS_ACCESSKEY);
        String amazonAWSSecretKey = testProperties.getProperty(Utilities.AWS_SECRETKEY);
        String amazonDynamoDBEndpoint = testProperties.getProperty(Utilities.DYNAMODB_ENDPOINT);

        amazonDynamoDB = new AmazonDynamoDBClient(new BasicAWSCredentials(amazonAWSAccessKey, amazonAWSSecretKey));
        amazonDynamoDB.setEndpoint(amazonDynamoDBEndpoint);
        dynamoDBMapperConfig = DynamoDBMapperConfig.builder()
                .withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT).withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES).build();
        dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB, dynamoDBMapperConfig);
        clock = Clock.systemUTC();
        outboundDynamoDAO = new OutboundDynamoDAOImpl(dynamoDBMapper);

        try {
            CreateTableRequest tableRequest = dynamoDBMapper.generateCreateTableRequest(FinishedGoodsOutbound.class);

            tableRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));

            amazonDynamoDB.createTable(tableRequest);
        } catch (ResourceInUseException e) {
        }
    }

    @After
    public void teardown() {
        DeleteTableRequest deleteTableRequest = dynamoDBMapper.generateDeleteTableRequest(FinishedGoodsOutbound.class, dynamoDBMapperConfig);
        amazonDynamoDB.deleteTable(deleteTableRequest);
    }

    @Test
    public void test_add_success() {
        long startTime = clock.millis();
        OutboundDTO outboundDTO = OutboundDTO.builder().outboundId(OUTBOUND_1).status(new Active()).warehouseId(WAREHOUSE_1)
                .customerId(CUSTOMER_ID).startTime(startTime).modifiedTime(startTime).userId(USER_ID).build();
        outboundDynamoDAO.add(outboundDTO);

        FinishedGoodsOutbound actualOutbound = dynamoDBMapper.load(FinishedGoodsOutbound.class, WAREHOUSE_1, OUTBOUND_1);

        new StringAssert(actualOutbound.getOutboundId()).isEqualTo(outboundDTO.getOutboundId());
        new StringAssert(actualOutbound.getWarehouseId()).isEqualTo(outboundDTO.getWarehouseId());
        new StringAssert(actualOutbound.getUserId()).isEqualTo(outboundDTO.getUserId());
        new StringAssert(actualOutbound.getCustomerId()).isEqualTo(outboundDTO.getCustomerId());
        new StringAssert(actualOutbound.getOutboundStatus().toString()).isEqualTo(outboundDTO.getStatus().toString());
        new LongAssert(actualOutbound.getEndTime()).isNull();
        new LongAssert(actualOutbound.getStartTime()).isEqualTo(outboundDTO.getStartTime());
        new LongAssert(actualOutbound.getModifiedTime()).isEqualTo(outboundDTO.getModifiedTime());
    }



    @Test
    public void test_add_already_existing() {

        long startTime = clock.millis();
        OutboundDTO outboundDTO = OutboundDTO.builder().outboundId(OUTBOUND_1).status(new Active()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(startTime).userId(USER_ID).customerId(CUSTOMER_ID).build();
        outboundDynamoDAO.add(outboundDTO);
        Assertions.assertThatExceptionOfType(ResourceAlreadyExistsException.class)
                .isThrownBy(() -> outboundDynamoDAO.add(outboundDTO)).withCauseExactlyInstanceOf(ConditionalCheckFailedException.class);
    }


    @Test
    public void test_add_all_different() {

        long startTime = clock.millis();
        OutboundDTO outboundDTO = OutboundDTO.builder().outboundId(OUTBOUND_1).status(new Active()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(startTime).userId(USER_ID).customerId(CUSTOMER_ID).build();
        OutboundDTO fgOutboundDTOSameWarehouseDiffOutbound = OutboundDTO.builder().outboundId(OUTBOUND_2).status(new Active()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(startTime).userId(USER_ID).customerId(CUSTOMER_ID).build();
        OutboundDTO fgOutboundDTODifferentWarehouseSameOutbound = OutboundDTO.builder().outboundId(OUTBOUND_1).status(new Active()).warehouseId(WAREHOUSE_2)
                .startTime(startTime).modifiedTime(startTime).userId(USER_ID).customerId(CUSTOMER_ID).build();

        outboundDynamoDAO.add(outboundDTO);
        outboundDynamoDAO.add(fgOutboundDTOSameWarehouseDiffOutbound);
        outboundDynamoDAO.add(fgOutboundDTODifferentWarehouseSameOutbound);
    }


    @Test
    public void test_update_already_existing() {

        long startTime = clock.millis();
        long endTime = startTime + 3;
        OutboundDTO outboundDTO = OutboundDTO.builder().outboundId(OUTBOUND_1).status(new Active()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(startTime).userId(USER_ID).customerId(CUSTOMER_ID).build();
        outboundDynamoDAO.add(outboundDTO);

        OutboundDTO updatedFGOutboundDTO = OutboundDTO.builder().outboundId(OUTBOUND_1).status(new Closed()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(endTime).endTime(endTime).userId(USER_ID).build();
        outboundDynamoDAO.update(updatedFGOutboundDTO);

        FinishedGoodsOutbound actualOutbound = dynamoDBMapper.load(FinishedGoodsOutbound.class, WAREHOUSE_1, OUTBOUND_1);

        new StringAssert(actualOutbound.getOutboundId()).isEqualTo(updatedFGOutboundDTO.getOutboundId());
        new StringAssert(actualOutbound.getWarehouseId()).isEqualTo(updatedFGOutboundDTO.getWarehouseId());
        new StringAssert(actualOutbound.getUserId()).isEqualTo(updatedFGOutboundDTO.getUserId());
        new StringAssert(actualOutbound.getOutboundStatus().toString()).isEqualTo(updatedFGOutboundDTO.getStatus().toString());
        new LongAssert(actualOutbound.getEndTime()).isEqualTo(updatedFGOutboundDTO.getEndTime());
        new LongAssert(actualOutbound.getStartTime()).isEqualTo(updatedFGOutboundDTO.getStartTime());
        new LongAssert(actualOutbound.getModifiedTime()).isEqualTo(updatedFGOutboundDTO.getModifiedTime());
    }


    @Test
    public void test_update_already_existing_no_status_update_attributes() {

        long startTime = clock.millis();
        long endTime = startTime + 3;
        OutboundDTO outboundDTO = OutboundDTO.builder().outboundId(OUTBOUND_1).status(new Active()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(startTime).userId(USER_ID).customerId(CUSTOMER_ID).build();
        outboundDynamoDAO.add(outboundDTO);

        OutboundDTO updatedOutboundDTO = OutboundDTO.builder().outboundId(OUTBOUND_1).warehouseId(WAREHOUSE_1)
                .modifiedTime(endTime).endTime(endTime).userId(USER_ID).build();
        outboundDynamoDAO.update(updatedOutboundDTO);

        FinishedGoodsOutbound actualOutbound = dynamoDBMapper.load(FinishedGoodsOutbound.class, WAREHOUSE_1, OUTBOUND_1);

        new StringAssert(actualOutbound.getOutboundId()).isEqualTo(updatedOutboundDTO.getOutboundId());
        new StringAssert(actualOutbound.getWarehouseId()).isEqualTo(updatedOutboundDTO.getWarehouseId());
        new StringAssert(actualOutbound.getUserId()).isEqualTo(updatedOutboundDTO.getUserId());
        new StringAssert(actualOutbound.getOutboundStatus().toString()).isEqualTo(outboundDTO.getStatus().toString());
        new LongAssert(actualOutbound.getEndTime()).isEqualTo(updatedOutboundDTO.getEndTime());
        new LongAssert(actualOutbound.getStartTime()).isEqualTo(outboundDTO.getStartTime());
        new LongAssert(actualOutbound.getModifiedTime()).isEqualTo(updatedOutboundDTO.getModifiedTime());
    }

    @Test
    public void test_update_not_existing() {

        long startTime = clock.millis();
        long endTime = startTime + 3;
        OutboundDTO outboundDTO = OutboundDTO.builder().outboundId(OUTBOUND_1).status(new Active()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(startTime).customerId(CUSTOMER_ID).userId(USER_ID).build();
        outboundDynamoDAO.add(outboundDTO);

        OutboundDTO updatedOutboundDTO = OutboundDTO.builder().outboundId(OUTBOUND_2).status(new Closed()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(endTime).endTime(endTime).userId(USER_ID).build();
        Assertions.assertThatExceptionOfType(NonRetriableException.class).isThrownBy(() -> outboundDynamoDAO.update(updatedOutboundDTO)).withCauseExactlyInstanceOf(ConditionalCheckFailedException.class);
    }

    @Test
    public void test_get_last_outbound_exists_success() {

        long startTime = clock.millis();
        OutboundDTO outboundDTO = OutboundDTO.builder().outboundId(OUTBOUND_1).status(new Active()).warehouseId(WAREHOUSE_1)
                .customerId(CUSTOMER_ID).startTime(startTime).modifiedTime(startTime).userId(USER_ID).build();

        OutboundDTO outboundDTO2 = OutboundDTO.builder().outboundId(OUTBOUND_2).status(new Active()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(startTime).userId(USER_ID).customerId(CUSTOMER_ID).build();
        outboundDynamoDAO.add(outboundDTO);
        outboundDynamoDAO.add(outboundDTO2);
        Optional<FinishedGoodsOutbound> lastOutboundOp = outboundDynamoDAO.getLastOutbound(WAREHOUSE_1);
        FinishedGoodsOutbound lastOutbound = lastOutboundOp.get();
        new StringAssert(lastOutbound.getOutboundId()).isEqualTo(outboundDTO2.getOutboundId());
        new StringAssert(lastOutbound.getWarehouseId()).isEqualTo(outboundDTO2.getWarehouseId());
        new StringAssert(lastOutbound.getUserId()).isEqualTo(outboundDTO2.getUserId());
        new StringAssert(lastOutbound.getOutboundStatus().toString()).isEqualTo(outboundDTO2.getStatus().toString());
        new LongAssert(lastOutbound.getEndTime()).isEqualTo(outboundDTO2.getEndTime());
        new LongAssert(lastOutbound.getStartTime()).isEqualTo(outboundDTO2.getStartTime());
        new LongAssert(lastOutbound.getModifiedTime()).isEqualTo(outboundDTO2.getModifiedTime());


    }

    @Test
    public void test_get_last_outbound_not_exists_success() {
        Optional<FinishedGoodsOutbound> lastOutbound = outboundDynamoDAO.getLastOutbound(WAREHOUSE_2);
        new BooleanAssert(lastOutbound.isPresent()).isEqualTo(false);
    }
}
