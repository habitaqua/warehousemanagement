package org.warehousenamagement.guice;

import com.google.inject.AbstractModule;

public class AWSModule extends AbstractModule {

    @Override
    protected void configure() {

        System.out.println("in aws module");
        System.out.println("bind s3");
       /* DynamoDBMapper dynamoDBMapper = provideBarcodesDynamoDbMapper();
        System.out.println("provided dynamodb mapper");
        bind(DynamoDBMapper.class).annotatedWith(Names.named("barcodesDdbMapper"))
                .toInstance(dynamoDBMapper);
 */       System.out.println("done");
    }


}
