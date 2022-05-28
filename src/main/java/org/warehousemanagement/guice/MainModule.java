package org.warehousemanagement.guice;

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
import org.warehousemanagement.service.barcodes.BarcodesPersistor;
import org.warehousemanagement.service.barcodes.S3BarcodesPersistor;

import java.io.IOException;
import java.time.Clock;
import java.util.Properties;

public class MainModule extends AbstractModule {
    @Override
    protected void configure() {
        bindProperties();
        bind(BarcodesPersistor.class).annotatedWith(Names.named("s3BarcodesPersistor")).to(S3BarcodesPersistor.class);

    }

    private void bindProperties() {
        String domain = System.getenv("domain");
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
    public DynamoDBMapper dynamoDBMapper() {


        AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.AP_SOUTH_1).build();
        System.out.println("provided dynamoodb");
        DynamoDBMapperConfig dynamoDBMapperConfig = DynamoDBMapperConfig.builder().withConsistentReads(
                DynamoDBMapperConfig.ConsistentReads.CONSISTENT)
                .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES).build();
        return new DynamoDBMapper(amazonDynamoDB, dynamoDBMapperConfig);
    }
}
