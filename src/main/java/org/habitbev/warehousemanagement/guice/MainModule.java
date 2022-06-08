package org.habitbev.warehousemanagement.guice;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import org.habitbev.warehousemanagement.dao.*;
import org.habitbev.warehousemanagement.guice.providers.WarehouseActionValidatorChainProvider;
import org.habitbev.warehousemanagement.helpers.BarcodesPersistor;
import org.habitbev.warehousemanagement.helpers.S3BarcodesPersistor;
import org.habitbev.warehousemanagement.helpers.idgenerators.*;
import org.habitbev.warehousemanagement.helpers.validators.WarehouseActionValidatorChain;
import org.habitbev.warehousemanagement.service.SKUService;

import java.io.IOException;
import java.time.Clock;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainModule extends AbstractModule {
    @Override
    protected void configure() {
        bindProperties();
        bind(ExecutorService.class).annotatedWith(Names.named("addInventoryExecutorService"))
                .toInstance(Executors.newFixedThreadPool(25));
        bind(BarcodesPersistor.class).annotatedWith(Names.named("s3BarcodesPersistor")).to(S3BarcodesPersistor.class);
        bindIdGenerators();
        bindDAO();
        bind(SKUService.class).annotatedWith(Names.named("configSKUService")).to(SKUService.class);
        bind(WarehouseActionValidatorChain.class).toProvider(WarehouseActionValidatorChainProvider.class);
    }

    private void bindDAO() {
        bind(ContainerCapacityDAO.class).annotatedWith(Names.named("dynamoDbImpl")).to(ContainerCapacityDynamoDAOImpl.class);
        bind(ContainerDAO.class).annotatedWith(Names.named("dynamoDbImpl")).to(ContainerDynamoDAOImpl.class);
        bind(InboundDAO.class).annotatedWith(Names.named("dynamoDbImpl")).to(InboundDynamoDAOImpl.class);
        bind(OutboundDAO.class).annotatedWith(Names.named("dynamoDbImpl")).to(OutboundDynamoDAOImpl.class);
        bind(InventoryDAO.class).annotatedWith(Names.named("dynamoDbImpl")).to(InventoryDynamoDAOImpl.class);
        bind(SKUDAO.class).annotatedWith(Names.named("configSKUDAOImpl")).to(ConfigSKUDAOImpl.class);
    }

    private void bindIdGenerators() {
        bind(ContainerIdGenerator.class).annotatedWith(Names.named("warehouseWiseIncrementalContainerIdGenerator")).to(WarehouseWiseIncrementalContainerIdGenerator.class);
        bind(InboundIdGenerator.class).annotatedWith(Names.named("warehouseWiseIncrementalInboundIdGenerator")).to(WarehouseWiseIncrementalInboundIdGenerator.class);
        bind(OutboundIdGenerator.class).annotatedWith(Names.named("warehouseWiseIncrementalOutboundIdGenerator")).to(WarehouseWiseIncrementalOutboundIdGenerator.class);
        bind(ProductIdGenerator.class).annotatedWith(Names.named("productionTimeBasedUniqueProductIdGenerator")).to(ProductionTimeBasedUniqueProductIdGenerator.class);
    }

    public static void main(String[] args) {
        new MainModule().configure();
    }
    private void bindProperties() {
        String domain = "test";
        Properties properties = new Properties();
        try {
            if ("prod".equals(domain)) {
                properties.load(getClass().getClassLoader().getResourceAsStream("prod.config"));
            } else {
                properties.load(getClass().getClassLoader().getResourceAsStream("test.config"));

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Names.bindProperties(binder(), properties);
    }

    @Provides
    public Gson providesGson() {
        return new GsonBuilder().setPrettyPrinting().create();
    }

    @Provides
    public ObjectMapper providesObjectMapper() {
        return new ObjectMapper();
    }

    @Provides
    public Clock providesClock() {
        return Clock.systemUTC();
    }

    @Provides
    public AmazonS3 provideAmazonS3() {
        return AmazonS3ClientBuilder.standard().withRegion(Regions.AP_SOUTH_1).build();
    }

    @Provides
    public AmazonDynamoDB amazonDynamoDbClient() {
        AmazonDynamoDB amazonDynamoDBClient = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.AP_SOUTH_1).build();
        return amazonDynamoDBClient;
    }

    @Provides
    public DynamoDBMapper dynamoDBMapper() {
        AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.AP_SOUTH_1).build();
        DynamoDBMapperConfig dynamoDBMapperConfig = DynamoDBMapperConfig.builder().withConsistentReads(
                        DynamoDBMapperConfig.ConsistentReads.CONSISTENT)
                .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES).build();
        return new DynamoDBMapper(amazonDynamoDB, dynamoDBMapperConfig);
    }
}
