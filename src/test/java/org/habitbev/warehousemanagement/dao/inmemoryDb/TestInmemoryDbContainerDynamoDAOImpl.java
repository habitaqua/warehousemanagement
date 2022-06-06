package org.habitbev.warehousemanagement.dao.inmemoryDb;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.model.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.*;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.habitbev.warehousemanagement.dao.ContainerDynamoDAOImpl;
import org.habitbev.warehousemanagement.entities.PaginatedResponse;
import org.habitbev.warehousemanagement.entities.container.ContainerDTO;
import org.habitbev.warehousemanagement.entities.container.GetContainerRequest;
import org.habitbev.warehousemanagement.entities.container.GetContainersRequest;
import org.habitbev.warehousemanagement.entities.dynamodb.Container;
import org.habitbev.warehousemanagement.entities.exceptions.ResourceAlreadyExistsException;
import org.habitbev.warehousemanagement.testutils.LocalDbCreationRule;
import org.habitbev.warehousemanagement.testutils.Utilities;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.time.Clock;
import java.util.*;

//TODO getContainers test case
public class TestInmemoryDbContainerDynamoDAOImpl {


    private static final String CONTAINER_1 = "CONTAINER-1";
    private static final String CONTAINER_2 = "CONTAINER-2";

    private static final String CONTAINER_3 = "CONTAINER-3";

    private static final Map<String, Integer> PREDEFINED_CAPACITY = ImmutableMap.of("sku1", 20);
    private static final String WAREHOUSE_1 = "WAREHOUSE-1";
    private static final String WAREHOUSE_2 = "WAREHOUSE-2";

    @ClassRule
    public static LocalDbCreationRule dynamoDB = new LocalDbCreationRule();
    ContainerDynamoDAOImpl containerDynamoDAO;
    DynamoDBMapper dynamoDBMapper;
    AmazonDynamoDB amazonDynamoDB;

    Clock clock;

    ObjectMapper objectMapper;
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
        objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        clock = Clock.systemUTC();
        containerDynamoDAO = new ContainerDynamoDAOImpl(dynamoDBMapper, clock, objectMapper);

        try {
            CreateTableRequest tableRequest = dynamoDBMapper.generateCreateTableRequest(Container.class);

            tableRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));

            amazonDynamoDB.createTable(tableRequest);
        } catch (ResourceInUseException e) {
        }
    }

    @After
    public void teardown() {
        DeleteTableRequest deleteTableRequest = dynamoDBMapper.generateDeleteTableRequest(Container.class, dynamoDBMapperConfig);
        amazonDynamoDB.deleteTable(deleteTableRequest);
    }

    @Test
    public void test_add_success() {

        ContainerDTO containerDTO = new ContainerDTO.Builder().containerId(CONTAINER_1).warehouseId(WAREHOUSE_1)
                .predefinedCapacity(PREDEFINED_CAPACITY).build();
        containerDynamoDAO.add(containerDTO);

        Container actualContainer = dynamoDBMapper.load(Container.class, WAREHOUSE_1, CONTAINER_1);

        new StringAssert(actualContainer.getContainerId()).isEqualTo(containerDTO.getContainerId());
        new StringAssert(actualContainer.getWarehouseId()).isEqualTo(actualContainer.getWarehouseId());
        new MapAssert<String, Integer>(actualContainer.getSkuCodeWisePredefinedCapacity())
                .isEqualTo(containerDTO.getSkuCodeWisePredefinedCapacity());
        new LongAssert(actualContainer.getCreationTime()).isNotNull();
        new LongAssert(actualContainer.getModifiedTime()).isNotNull();
    }


    @Test
    public void test_add_already_existing() {


        ContainerDTO containerDTO = new ContainerDTO.Builder().containerId(CONTAINER_1).warehouseId(WAREHOUSE_1)
                .predefinedCapacity(PREDEFINED_CAPACITY).build();
        containerDynamoDAO.add(containerDTO);

        Assertions.assertThatExceptionOfType(ResourceAlreadyExistsException.class)
                .isThrownBy(() -> containerDynamoDAO.add(containerDTO)).withCauseExactlyInstanceOf(ConditionalCheckFailedException.class);
    }


    @Test
    public void test_add_all_different() {

        ContainerDTO containerDTO = new ContainerDTO.Builder().containerId(CONTAINER_1).warehouseId(WAREHOUSE_1)
                .predefinedCapacity(PREDEFINED_CAPACITY).build();
        ContainerDTO containerDTOSameWarehouseDiffContainer = new ContainerDTO.Builder().containerId(CONTAINER_2).warehouseId(WAREHOUSE_1)
                .predefinedCapacity(PREDEFINED_CAPACITY).build();
        ContainerDTO containerDTODifferentWarehouseSameContainer = new ContainerDTO.Builder().containerId(CONTAINER_1).warehouseId(WAREHOUSE_2)
                .predefinedCapacity(PREDEFINED_CAPACITY).build();

        containerDynamoDAO.add(containerDTO);
        containerDynamoDAO.add(containerDTOSameWarehouseDiffContainer);
        containerDynamoDAO.add(containerDTODifferentWarehouseSameContainer);
    }


    @Test
    public void test_get_existing_container() {

        ContainerDTO expected = new ContainerDTO.Builder().containerId(CONTAINER_1).warehouseId(WAREHOUSE_1)
                .predefinedCapacity(PREDEFINED_CAPACITY).build();

        containerDynamoDAO.add(expected);
        Optional<ContainerDTO> containerOp = containerDynamoDAO.getContainer(GetContainerRequest.builder().warehouseId(expected.getWarehouseId())
                .containerId(expected.getContainerId()).build());
        new BooleanAssert(containerOp.isPresent()).isEqualTo(true);
        ContainerDTO actual = containerOp.get();
        Assertions.assertThat(expected).usingRecursiveComparison(RecursiveComparisonConfiguration.builder()
                .withComparedFields("skuCodeWisePredefinedCapacity", "containerId","warehouseId").build()).isEqualTo(actual);

    }

    @Test
    public void test_get_non_existing_container() {
        Optional<ContainerDTO> containerOp = containerDynamoDAO.getContainer(GetContainerRequest.builder().warehouseId(WAREHOUSE_1)
                .containerId(CONTAINER_1).build());
        new BooleanAssert(containerOp.isPresent()).isEqualTo(false);
    }

   // @Test
    public void test_get_existing_containers() {

        ContainerDTO container1 = new ContainerDTO.Builder().containerId(CONTAINER_1).warehouseId(WAREHOUSE_1)
                .predefinedCapacity(PREDEFINED_CAPACITY).build();

        ContainerDTO container2 = new ContainerDTO.Builder().containerId(CONTAINER_2).warehouseId(WAREHOUSE_1)
                .predefinedCapacity(PREDEFINED_CAPACITY).build();

        ContainerDTO container3 = new ContainerDTO.Builder().containerId(CONTAINER_3).warehouseId(WAREHOUSE_1)
                .predefinedCapacity(PREDEFINED_CAPACITY).build();

        containerDynamoDAO.add(container1);
        containerDynamoDAO.add(container2);
        containerDynamoDAO.add(container3);

        List<ContainerDTO> responseContainers = new ArrayList<>();
        String nextPageToken = null;
        do{
            GetContainersRequest getContainersRequest = GetContainersRequest.builder().warehouseId(WAREHOUSE_1).limit(1).pageToken(nextPageToken).build();
            PaginatedResponse<ContainerDTO> containers = containerDynamoDAO.getContainers(getContainersRequest);
            responseContainers.addAll(containers.getItems());
            nextPageToken = containers.getNextPageToken();

        }
        while (nextPageToken!=null);
        new IntegerAssert(responseContainers.size()).isEqualTo(3);
    }


    @Test
    public void test_get_last_container_exists_success() {

        ContainerDTO container1 = new ContainerDTO.Builder().containerId(CONTAINER_1).warehouseId(WAREHOUSE_1)
                .predefinedCapacity(PREDEFINED_CAPACITY).build();

        ContainerDTO container2 = new ContainerDTO.Builder().containerId(CONTAINER_2).warehouseId(WAREHOUSE_1)
                .predefinedCapacity(PREDEFINED_CAPACITY).build();
        containerDynamoDAO.add(container1);
        containerDynamoDAO.add(container2);
        Optional<ContainerDTO> containerOp = containerDynamoDAO.getLastAddedContainer(WAREHOUSE_1);
        ContainerDTO containerDTO = containerOp.get();
        Assertions.assertThat(containerDTO).usingRecursiveComparison(RecursiveComparisonConfiguration.builder()
                .withComparedFields("skuCodeWisePredefinedCapacity", "containerId","warehouseId").build()).isEqualTo(container2);
    }

    @Test
    public void test_get_last_inbound_not_exists_success() {
        Optional<ContainerDTO> containerOp = containerDynamoDAO.getLastAddedContainer(WAREHOUSE_1);
        new BooleanAssert(containerOp.isPresent()).isEqualTo(false);
    }

}
