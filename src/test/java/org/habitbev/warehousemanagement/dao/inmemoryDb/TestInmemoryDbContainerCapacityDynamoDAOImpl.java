package org.habitbev.warehousemanagement.dao.inmemoryDb;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.model.*;
import org.assertj.core.api.*;
import org.habitbev.warehousemanagement.entities.container.containerstatus.Available;
import org.habitbev.warehousemanagement.entities.container.containerstatus.PartiallyFilled;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.habitbev.warehousemanagement.dao.ContainerCapacityDynamoDAOImpl;
import org.habitbev.warehousemanagement.entities.dynamodb.ContainerCapacity;
import org.habitbev.warehousemanagement.entities.exceptions.ResourceAlreadyExistsException;
import org.habitbev.warehousemanagement.testutils.LocalDbCreationRule;
import org.habitbev.warehousemanagement.testutils.Utilities;

import java.time.Clock;
import java.util.Optional;
import java.util.Properties;

import static org.habitbev.warehousemanagement.testutils.Utilities.*;

//TODO getContainers test case
public class TestInmemoryDbContainerCapacityDynamoDAOImpl {


    private static final String CONTAINER_1 = "CONTAINER-1";


    private static final String WAREHOUSE_1 = "WAREHOUSE-1";
    private static final String DELIMITER = "<%>";

    @ClassRule
    public static LocalDbCreationRule dynamoDB = new LocalDbCreationRule();
    ContainerCapacityDynamoDAOImpl containerCapacityDynamoDAO;
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
        clock = Clock.systemUTC();
        containerCapacityDynamoDAO = new ContainerCapacityDynamoDAOImpl(dynamoDBMapper, clock);

        try {
            CreateTableRequest tableRequest = dynamoDBMapper.generateCreateTableRequest(ContainerCapacity.class);

            tableRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));

            amazonDynamoDB.createTable(tableRequest);
        } catch (ResourceInUseException e) {
        }
    }

    @After
    public void teardown() {
        DeleteTableRequest deleteTableRequest = dynamoDBMapper.generateDeleteTableRequest(ContainerCapacity.class, dynamoDBMapperConfig);
        amazonDynamoDB.deleteTable(deleteTableRequest);
    }

    @Test
    public void test_get_present_success() {
        long millis = clock.millis();
        ContainerCapacity containerCapacityExpected = ContainerCapacity.builder().warehouseContainerId(String.join(DELIMITER, WAREHOUSE_1, CONTAINER_1))
                .modifiedTime(millis).currentCapacity(10).creationTime(millis).containerStatus(new PartiallyFilled()).build();
        dynamoDBMapper.save(containerCapacityExpected);
        Optional<ContainerCapacity> containerCapacityActualOp = containerCapacityDynamoDAO.get(WAREHOUSE_1, CONTAINER_1);
        new BooleanAssert(containerCapacityActualOp.isPresent()).isEqualTo(true);
        Assertions.assertThat(containerCapacityExpected).usingRecursiveComparison().isEqualTo(containerCapacityActualOp.get());
    }

    @Test
    public void test_get_not_present_success() {
               Optional<ContainerCapacity> containerCapacityActualOp = containerCapacityDynamoDAO.get(WAREHOUSE_1, CONTAINER_1);
        new BooleanAssert(containerCapacityActualOp.isPresent()).isEqualTo(false);
    }


    @Test
    public void test_init_success() {
        containerCapacityDynamoDAO.init(WAREHOUSE_1, CONTAINER_1);
        ContainerCapacity actualContainerCapacity = dynamoDBMapper.load(ContainerCapacity.class, String.join(DELIMITER, WAREHOUSE_1, CONTAINER_1));
        String[] warehouseContainerId = actualContainerCapacity.getWarehouseContainerId().split(DELIMITER);
        new StringAssert(warehouseContainerId[0]).isEqualTo(WAREHOUSE_1);
        new StringAssert(warehouseContainerId[1]).isEqualTo(CONTAINER_1);
        new IntegerAssert(actualContainerCapacity.getCurrentCapacity()).isEqualTo(0);
        new StringAssert(actualContainerCapacity.getContainerStatus().toString()).isEqualTo(new Available().toString());
        new IntegerAssert(warehouseContainerId.length).isEqualTo(2);
        new LongAssert(actualContainerCapacity.getCreationTime()).isNotNull();
        new LongAssert(actualContainerCapacity.getModifiedTime()).isNotNull();
    }

    @Test
    public void test_init_already_initialised() {
        containerCapacityDynamoDAO.init(WAREHOUSE_1, CONTAINER_1);
        Assertions.assertThatExceptionOfType(ResourceAlreadyExistsException.class).isThrownBy(() ->
                containerCapacityDynamoDAO.init(WAREHOUSE_1, CONTAINER_1)).withCauseExactlyInstanceOf(ConditionalCheckFailedException.class);
    }
}
