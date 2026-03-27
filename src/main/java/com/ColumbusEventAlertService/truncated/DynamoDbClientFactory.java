package com.ColumbusEventAlertService.truncated;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class DynamoDbClientFactory {
    public static DynamoDbClient createClient() {
        return DynamoDbClient.builder()
                .credentialsProvider(isRunningLocally()
                        ? ProfileCredentialsProvider.create("local") // Use "local" profile when running locally
                        : DefaultCredentialsProvider.create()) // Use default AWS credentials in Lambda
                .region(Region.US_EAST_1) // Set your AWS region
                .build();
    }

    private static boolean isRunningLocally() {
        return System.getenv("AWS_EXECUTION_ENV") == null;
        // AWS Lambda sets this environment variable, so if it's null, we're running locally
    }
}
