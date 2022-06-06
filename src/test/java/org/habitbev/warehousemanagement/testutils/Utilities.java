package org.habitbev.warehousemanagement.testutils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

public class Utilities {

    public static final String DYNAMODB_ENDPOINT = "amazon.dynamodb.endpoint";
    public static final String AWS_ACCESSKEY = "amazon.aws.accesskey";
    public static final String AWS_SECRETKEY = "amazon.aws.secretkey";

    public static Optional<Properties> loadFromFileInClasspath(String fileName) {
        InputStream stream = null;
        try {
            Properties config = new Properties();
            Path configLocation = Paths.get(ClassLoader.getSystemResource(fileName).toURI());
            stream = Files.newInputStream(configLocation);
            config.load(stream);
            return Optional.of(config);
        } catch (Exception e) {
            return Optional.empty();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static Properties getTestProperties() {
        Properties testProperties = Utilities.loadFromFileInClasspath("test.properties")
                .filter(properties -> !isEmpty(properties.getProperty(AWS_ACCESSKEY)))
                .filter(properties -> !isEmpty(properties.getProperty(AWS_SECRETKEY)))
                .filter(properties -> !isEmpty(properties.getProperty(DYNAMODB_ENDPOINT)))
                .orElseThrow(() -> new RuntimeException("Unable to get all of the required test property values"));
        return testProperties;

    }
}
