package com.example.demo.services;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.example.demo.entity.EC2Instance;
import com.example.demo.entity.S3Buckets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class FileService {

    private static final Logger LOG = LoggerFactory.getLogger(FileService.class);

    @Autowired
    private S3Client s3Client;

    @Autowired
    private AmazonEC2 ec2Client;

    @Value("${s3.bucket.name}")
    private String s3BucketName;

    // @Async annotation ensures that the method is executed in a different thread

    @Async
    public List<EC2Instance> getEC2Service(String serviceName) {
        // Logic to get a list of EC2 instance IDs
        DescribeInstancesResult response = ec2Client.describeInstances();
        List<Instance> instances = response.getReservations().stream()
                .flatMap(reservation -> reservation.getInstances().stream())
                .toList();
        return mapToEC2Instance(instances);
    }

    private List<EC2Instance> mapToEC2Instance(List<Instance> instances) {
        List<EC2Instance> ec2Instances = new ArrayList<>();
        for (Instance instance:instances) {
            EC2Instance ec2Instance = new EC2Instance();
            ec2Instance.setInstanceId(instance.getInstanceId());
            ec2Instance.setInstanceType(instance.getInstanceType());;
            ec2Instances.add(ec2Instance);
        }
        return ec2Instances;
    }

    @Async
    public List<S3Buckets> getS3Service(String serviceName) {
        ListBucketsResponse buckets = s3Client.listBuckets();
        List<Bucket> s3Buckets = buckets.buckets().stream()
                .collect(Collectors.toList());
        return mapToS3Bucket(s3Buckets);
    }

    private List<S3Buckets> mapToS3Bucket(List<Bucket> s3Buckets) {
        List<S3Buckets> buckets = new ArrayList<>();
        for (Bucket bucket : s3Buckets) {
            S3Buckets s3Bucket = new S3Buckets();
            s3Bucket.setBucketName(bucket.name());
            buckets.add(s3Bucket);
        }
        return buckets;
    }

    @Async
    public Object findServiceByName(String serviceName) {
        if ("S3".equals(serviceName)) {
            // Logic to get a list of S3 buckets
            return getS3Buckets();
        } else if ("EC2".equals(serviceName)) {
            // Logic to get a list of EC2 instance IDs
            List<String> ec2InstanceIds = getEC2InstanceIds();
            return ec2InstanceIds;
        } else {
            return "Unsupported service";
        }
    }

    @Async
    public Object findByName(String bucketName, String pattern) {
        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();
        ListObjectsV2Response listObjectsV2Response = s3Client.listObjectsV2(listObjectsV2Request);

        List<S3Object> contents = listObjectsV2Response.contents();

        System.out.println("Number of objects in the bucket: " + contents.stream().count());
        if (Objects.nonNull(pattern)) {
            return contents.stream().map(S3Object::key).filter(s -> s.contains(pattern)).toList();
        }
        return contents.stream().map(S3Object::key).toList();
    }

    @Async
    public Integer findCountByName(String bucketName, String pattern) {
        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();
        ListObjectsV2Response listObjectsV2Response = s3Client.listObjectsV2(listObjectsV2Request);

        List<S3Object> contents = listObjectsV2Response.contents();
        if (Objects.nonNull(pattern)) {
            return contents.stream().map(S3Object::key).filter(s -> s.contains(pattern)).toList().size();
        }
        System.out.println("Number of objects in the bucket: " + contents.stream().count());
        contents.stream().forEach(System.out::println);
        return contents.size();
    }

    private List<String> getS3Buckets() {
        ListBucketsResponse buckets = s3Client.listBuckets();

        // Extract the bucket names from the response
        List<String> s3Buckets = buckets.buckets().stream()
                .map(bucket -> bucket.name())
                .collect(Collectors.toList());

        // Print the list of S3 buckets
        for (String bucketName : s3Buckets) {
            System.out.println("S3 Bucket: " + bucketName);
        }
        return s3Buckets;
    }

    private List<String> getEC2InstanceIds() {
        try {
            DescribeInstancesResult response = ec2Client.describeInstances();
            List<String> instances = response.getReservations().stream()
                    .flatMap(reservation -> reservation.getInstances().stream())
                    .map(Instance::getInstanceId)
                    .toList();

            // Print the list of EC2 instances
            for (String instance : instances) {
                System.out.println("EC2 Instance : " + instance);
            }
            return instances;
        } catch (Ec2Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
