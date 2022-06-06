package org.habitbev.warehousemanagement.service.barcodes;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.util.UUID;

@Slf4j
public class S3BarcodesPersistor implements BarcodesPersistor {

    AmazonS3 amazonS3;
    String barcodesBucketName;

    @Inject
    public S3BarcodesPersistor(AmazonS3 amazonS3, @Named("barcodesBucketName") String barcodesBucketName) {
        this.amazonS3 = amazonS3;
        this.barcodesBucketName = barcodesBucketName;
    }


    @Override public String persistBarcodeFile(String filePath) {

        try {

            FileInputStream fileInputStream = new FileInputStream(filePath);

            String barcodesUniqueFileName = UUID.randomUUID().toString();
            System.out.println("bucket name " + barcodesBucketName);
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType("application/pdf");
            PutObjectRequest putObjectRequest = new PutObjectRequest(barcodesBucketName, barcodesUniqueFileName,
                    fileInputStream, objectMetadata);
            amazonS3.putObject(putObjectRequest);
            String resourceUrl = ((AmazonS3Client) amazonS3).getResourceUrl(barcodesBucketName, barcodesUniqueFileName);
            System.out.println("resource url" + resourceUrl);
            return resourceUrl;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
