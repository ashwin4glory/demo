package com.example.demo.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class FileConfiguration {

    @Value("${access.key.id}")
    private String accessKeyId;

    @Value("${access.key.secret}")
    private String accessKeySecret;

    @Value("${s3.region.name}")
    private String regionName;

    @Bean
    public AmazonEC2 ec2Client() {

        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKeyId, accessKeySecret);
        // Create an EC2 client using AmazonEC2ClientBuilder
        return AmazonEC2ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(regionName)
                .build();
    }

    @Bean
    public S3Client getAmazonS3Client() {
        return S3Client.builder()
                .credentialsProvider(() -> AwsBasicCredentials.create(accessKeyId, accessKeySecret))
                .region(Region.of(regionName))
                .build();
    }


}
