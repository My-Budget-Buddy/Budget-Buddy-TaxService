package com.skillstorm.taxservice.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Bean
    public S3Client s3() {

        // Create a session token for temporary credentials. Ideally you would set up an endpoint to your bucket in your
        // VPC so you don't have to use temporary credentials, but this will work when testing locally:
        //AwsSessionCredentials awsCreds = AwsSessionCredentials.create("${ACCESS_KEY}", "${SECRET_ACCESS_KEY}", "${SESSION_TOKEN}");

        // Bucket names are globally unique. The bucket you try to access must be within
        // the region specified here:
        return S3Client.builder()
                .region(Region.US_EAST_1)
                //.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
    }
}
