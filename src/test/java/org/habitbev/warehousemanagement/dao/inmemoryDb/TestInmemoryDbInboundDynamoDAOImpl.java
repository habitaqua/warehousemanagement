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
import org.habitbev.warehousemanagement.dao.InboundDynamoDAOImpl;
import org.habitbev.warehousemanagement.entities.dynamodb.FinishedGoodsInbound;
import org.habitbev.warehousemanagement.entities.exceptions.NonRetriableException;
import org.habitbev.warehousemanagement.entities.exceptions.ResourceAlreadyExistsException;
import org.habitbev.warehousemanagement.entities.inbound.FGInboundDTO;
import org.habitbev.warehousemanagement.entities.inbound.inboundstatus.Active;
import org.habitbev.warehousemanagement.entities.inbound.inboundstatus.Closed;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.habitbev.warehousemanagement.testutils.LocalDbCreationRule;
import org.habitbev.warehousemanagement.testutils.Utilities;

import java.time.Clock;
import java.util.Optional;
import java.util.Properties;

import static org.habitbev.warehousemanagement.testutils.Utilities.*;


public class TestInmemoryDbInboundDynamoDAOImpl {


    private static final String INBOUND_1 = "INBOUND-1";
    private static final String INBOUND_2 = "INBOUND-2";

    private static final String WAREHOUSE_1 = "WAREHOUSE-1";
    private static final String WAREHOUSE_2 = "WAREHOUSE-2";

    private static final String USER_ID = "user-1";

    @ClassRule
    public static LocalDbCreationRule dynamoDB = new LocalDbCreationRule();
    InboundDynamoDAOImpl inboundDbSAO;
    DynamoDBMapper dynamoDBMapper;
    AmazonDynamoDB amazonDynamoDB;

    Clock clock;

    DynamoDBMapperConfig dynamoDBMapperConfig;


    @Before
    public void setup() {
        Properties testProperties = Utilities.getTestProperties();
        String amazonAWSAccessKey = testProperties.getProperty(AWS_ACCESSKEY);
        String amazonAWSSecretKey = testProperties.getProperty(AWS_SECRETKEY);
        String amazonDynamoDBEndpoint = testProperties.getProperty(DYNAMODB_ENDPOINT);

        amazonDynamoDB = new AmazonDynamoDBClient(new BasicAWSCredentials(amazonAWSAccessKey, amazonAWSSecretKey));
        amazonDynamoDB.setEndpoint(amazonDynamoDBEndpoint);
        dynamoDBMapperConfig = DynamoDBMapperConfig.builder()
                .withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT).withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES).build();
        dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB, dynamoDBMapperConfig);
        inboundDbSAO = new InboundDynamoDAOImpl(dynamoDBMapper);
        clock = Clock.systemUTC();
        try {
            CreateTableRequest tableRequest = dynamoDBMapper.generateCreateTableRequest(FinishedGoodsInbound.class);

            tableRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));

            amazonDynamoDB.createTable(tableRequest);
        } catch (ResourceInUseException e) {
        }
    }

    @After
    public void teardown() {
        DeleteTableRequest deleteTableRequest = dynamoDBMapper.generateDeleteTableRequest(FinishedGoodsInbound.class, dynamoDBMapperConfig);
        amazonDynamoDB.deleteTable(deleteTableRequest);
    }

    @Test
    public void test_add_success() {
        long startTime = clock.millis();
        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).status(new Active()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(startTime).userId(USER_ID).build();
        inboundDbSAO.add(fgInboundDTO);

        FinishedGoodsInbound actualInbound = dynamoDBMapper.load(FinishedGoodsInbound.class, WAREHOUSE_1, INBOUND_1);

        new StringAssert(actualInbound.getInboundId()).isEqualTo(fgInboundDTO.getInboundId());
        new StringAssert(actualInbound.getWarehouseId()).isEqualTo(fgInboundDTO.getWarehouseId());
        new StringAssert(actualInbound.getUserId()).isEqualTo(fgInboundDTO.getUserId());
        new StringAssert(actualInbound.getInboundStatus().toString()).isEqualTo(fgInboundDTO.getStatus().toString());
        new LongAssert(actualInbound.getEndTime()).isNull();
        new LongAssert(actualInbound.getStartTime()).isEqualTo(fgInboundDTO.getStartTime());
        new LongAssert(actualInbound.getModifiedTime()).isEqualTo(fgInboundDTO.getModifiedTime());
    }


    @Test
    public void test_add_already_existing() {

        long startTime = clock.millis();
        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).status(new Active()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(startTime).userId(USER_ID).build();
        inboundDbSAO.add(fgInboundDTO);
        Assertions.assertThatExceptionOfType(ResourceAlreadyExistsException.class)
                .isThrownBy(() -> inboundDbSAO.add(fgInboundDTO)).withCauseExactlyInstanceOf(ConditionalCheckFailedException.class);
    }


    @Test
    public void test_add_all_different() {

        long startTime = clock.millis();
        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).status(new Active()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(startTime).userId(USER_ID).build();
        FGInboundDTO fgInboundDTOSameWarehouseDiffInbound = FGInboundDTO.builder().inboundId(INBOUND_2).status(new Active()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(startTime).userId(USER_ID).build();
        FGInboundDTO fgInboundDTODifferentWarehouseSameInbound = FGInboundDTO.builder().inboundId(INBOUND_1).status(new Active()).warehouseId(WAREHOUSE_2)
                .startTime(startTime).modifiedTime(startTime).userId(USER_ID).build();

        inboundDbSAO.add(fgInboundDTO);
        inboundDbSAO.add(fgInboundDTOSameWarehouseDiffInbound);
        inboundDbSAO.add(fgInboundDTODifferentWarehouseSameInbound);
    }

    @Test
    public void test_update_already_existing() {

        long startTime = clock.millis();
        long endTime = startTime + 3;
        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).status(new Active()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(startTime).userId(USER_ID).build();
        inboundDbSAO.add(fgInboundDTO);

        FGInboundDTO updatedFGInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).status(new Closed()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(endTime).endTime(endTime).userId(USER_ID).build();
        inboundDbSAO.update(updatedFGInboundDTO);

        FinishedGoodsInbound actualInbound = dynamoDBMapper.load(FinishedGoodsInbound.class, WAREHOUSE_1, INBOUND_1);

        new StringAssert(actualInbound.getInboundId()).isEqualTo(updatedFGInboundDTO.getInboundId());
        new StringAssert(actualInbound.getWarehouseId()).isEqualTo(updatedFGInboundDTO.getWarehouseId());
        new StringAssert(actualInbound.getUserId()).isEqualTo(updatedFGInboundDTO.getUserId());
        new StringAssert(actualInbound.getInboundStatus().toString()).isEqualTo(updatedFGInboundDTO.getStatus().toString());
        new LongAssert(actualInbound.getEndTime()).isEqualTo(updatedFGInboundDTO.getEndTime());
        new LongAssert(actualInbound.getStartTime()).isEqualTo(updatedFGInboundDTO.getStartTime());
        new LongAssert(actualInbound.getModifiedTime()).isEqualTo(updatedFGInboundDTO.getModifiedTime());
    }

    @Test
    public void test_update_already_existing_some_attributes() {

        long startTime = clock.millis();
        long endTime = startTime + 3;
        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).status(new Active()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(startTime).userId(USER_ID).build();
        inboundDbSAO.add(fgInboundDTO);

        FGInboundDTO updatedFGInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).warehouseId(WAREHOUSE_1)
                .modifiedTime(endTime).endTime(endTime).userId(USER_ID).build();
        inboundDbSAO.update(updatedFGInboundDTO);

        FinishedGoodsInbound actualInbound = dynamoDBMapper.load(FinishedGoodsInbound.class, WAREHOUSE_1, INBOUND_1);

        new StringAssert(actualInbound.getInboundId()).isEqualTo(updatedFGInboundDTO.getInboundId());
        new StringAssert(actualInbound.getWarehouseId()).isEqualTo(updatedFGInboundDTO.getWarehouseId());
        new StringAssert(actualInbound.getUserId()).isEqualTo(updatedFGInboundDTO.getUserId());
        new StringAssert(actualInbound.getInboundStatus().toString()).isEqualTo(fgInboundDTO.getStatus().toString());
        new LongAssert(actualInbound.getEndTime()).isEqualTo(updatedFGInboundDTO.getEndTime());
        new LongAssert(actualInbound.getStartTime()).isEqualTo(fgInboundDTO.getStartTime());
        new LongAssert(actualInbound.getModifiedTime()).isEqualTo(updatedFGInboundDTO.getModifiedTime());
    }

    @Test
    public void test_update_not_existing() {

        long startTime = clock.millis();
        long endTime = startTime + 3;
        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).status(new Active()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(startTime).userId(USER_ID).build();
        inboundDbSAO.add(fgInboundDTO);

        FGInboundDTO updatedFGInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_2).status(new Closed()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(endTime).endTime(endTime).userId(USER_ID).build();
        Assertions.assertThatExceptionOfType(NonRetriableException.class).isThrownBy(() -> inboundDbSAO.update(updatedFGInboundDTO)).withCauseExactlyInstanceOf(ConditionalCheckFailedException.class);
    }

    @Test
    public void test_get_last_inbound_exists_success() {

        long startTime = clock.millis();
        FGInboundDTO fgInboundDTO = FGInboundDTO.builder().inboundId(INBOUND_1).status(new Active()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(startTime).userId(USER_ID).build();

        FGInboundDTO fgInboundDTO2 = FGInboundDTO.builder().inboundId(INBOUND_2).status(new Active()).warehouseId(WAREHOUSE_1)
                .startTime(startTime).modifiedTime(startTime).userId(USER_ID).build();
        inboundDbSAO.add(fgInboundDTO);
        inboundDbSAO.add(fgInboundDTO2);
        Optional<FinishedGoodsInbound> lastInboundOp = inboundDbSAO.getLastInbound(WAREHOUSE_1);
        FinishedGoodsInbound lastInbound = lastInboundOp.get();
        new StringAssert(lastInbound.getInboundId()).isEqualTo(fgInboundDTO2.getInboundId());
        new StringAssert(lastInbound.getWarehouseId()).isEqualTo(fgInboundDTO2.getWarehouseId());
        new StringAssert(lastInbound.getUserId()).isEqualTo(fgInboundDTO2.getUserId());
        new StringAssert(lastInbound.getInboundStatus().toString()).isEqualTo(fgInboundDTO2.getStatus().toString());
        new LongAssert(lastInbound.getEndTime()).isEqualTo(fgInboundDTO2.getEndTime());
        new LongAssert(lastInbound.getStartTime()).isEqualTo(fgInboundDTO2.getStartTime());
        new LongAssert(lastInbound.getModifiedTime()).isEqualTo(fgInboundDTO2.getModifiedTime());


    }

    @Test
    public void test_get_last_inbound_not_exists_success() {
        Optional<FinishedGoodsInbound> lastInbound = inboundDbSAO.getLastInbound(WAREHOUSE_2);
        new BooleanAssert(lastInbound.isPresent()).isEqualTo(false);
    }


}
