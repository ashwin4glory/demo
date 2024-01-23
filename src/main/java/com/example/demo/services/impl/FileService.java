package com.example.demo.services.impl;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.example.demo.dao.EC2InstanceRepository;
import com.example.demo.dao.JobRepository;
import com.example.demo.dao.S3BucketFilesRepository;
import com.example.demo.dao.S3BucketsRepository;
import com.example.demo.entity.*;
import com.example.demo.exceptionHandler.GlobalExceptionHandler;
import com.example.demo.request.ServiceRequest;
import com.example.demo.response.ServiceResponse;
import com.example.demo.services.IFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@EnableAsync(proxyTargetClass = true)
public class FileService implements IFileService {

    @Autowired
    private S3Client s3Client;

    @Autowired
    private AmazonEC2 ec2Client;

    @Autowired
    private EC2InstanceRepository ec2InstanceRepository;

    @Autowired
    private S3BucketsRepository s3BucketsRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private S3BucketFilesRepository s3BucketFilesRepository;

    @Async
    @Override
    public void getService(ServiceRequest serviceRequest, Job job) {
        try {
            List<S3Buckets> s3Buckets = new ArrayList<>();
            List<EC2Instance> ec2Instances = new ArrayList<>();
            job.setS3Buckets(s3Buckets);
            job.setEc2Instances(ec2Instances);
            List<CompletableFuture> data = new ArrayList<>();
            for (String serviceName : serviceRequest.getServices()) {
                data.add(performOperation(serviceName));
            }
            CompletableFuture.allOf(data.toArray(new CompletableFuture[0])).join();
            for (CompletableFuture completableFuture : data) {
                Object obj = completableFuture.get();
                if (obj instanceof List) {
                    if (((List<?>) obj).get(0) instanceof EC2Instance) {
                        ec2Instances = (List<EC2Instance>) obj;
                    } else if (((List<?>) obj).get(0) instanceof S3Buckets) {
                        s3Buckets = (List<S3Buckets>) obj;
                    }
                }

            }
            if (!ec2Instances.isEmpty()) {
                job.setEc2Instances(ec2Instances);
            }
            if (!s3Buckets.isEmpty()) {
                job.setS3Buckets(s3Buckets);
            }
            job.setS3BucketFiles(new ArrayList<>());
            job.setStatus(Status.SUCCESS);
            jobRepository.save(job);

        } catch(Exception e) {
            log.error(e.getMessage());
            log.info("Failed in completing the job, marking the job status to FAILED");
            job.setStatus(Status.FAILED);
            job.setS3Buckets(new ArrayList<>());
            job.setEc2Instances(new ArrayList<>());
            jobRepository.save(job);
        }
    }

    @Async
    public CompletableFuture<Object> performOperation(String serviceName) {
        if ("S3".equals(serviceName)) {
            // Logic to get a list of S3 buckets
            ListBucketsResponse buckets = s3Client.listBuckets();
            List<S3Buckets> s3Buckets = mapToS3Bucket(buckets.buckets().stream()
                    .collect(Collectors.toList()));
            return CompletableFuture.completedFuture(s3Buckets);
        } else if ("EC2".equals(serviceName)) {
            // Logic to get a list of EC2 instance IDs
            DescribeInstancesResult response = ec2Client.describeInstances();
            List<Instance> instances = response.getReservations().stream()
                    .flatMap(reservation -> reservation.getInstances().stream())
                    .toList();
            List<EC2Instance> ec2Instances = mapToEC2Instance(instances);
            return CompletableFuture.completedFuture(ec2Instances);
        }
        return null;
    }

    private List<EC2Instance> mapToEC2Instance(List<Instance> instances) {
        List<EC2Instance> ec2Instances = new ArrayList<>();
        for (Instance instance:instances) {
            EC2Instance ec2Instance = new EC2Instance();
            ec2Instance.setInstanceId(instance.getInstanceId());
            ec2Instance.setInstanceType(instance.getInstanceType());;
            ec2Instance.setDescription(instance.toString());
            ec2Instances.add(ec2Instance);
        }
        return ec2Instances;
    }

    private List<S3Buckets> mapToS3Bucket(List<Bucket> s3Buckets) {
        List<S3Buckets> buckets = new ArrayList<>();
        for (Bucket bucket : s3Buckets) {
            S3Buckets s3Bucket = new S3Buckets();
            s3Bucket.setS3Id(Long.parseLong(String.valueOf(bucket.hashCode())));
            s3Bucket.setBucketName(bucket.name());
            s3Bucket.setDescription(bucket.toString());
            buckets.add(s3Bucket);
        }
        return buckets;
    }

    @Override
    public Status getJobStatusById(Long jobId) {
        try {
            return jobRepository.findById(jobId).get().getStatus();
        } catch(Exception e) {
            throw new GlobalExceptionHandler(e.getMessage());
        }
    }

    @Override
    public ServiceResponse findServiceByName(String serviceName) {
        ServiceResponse serviceResponse = new ServiceResponse();
        if ("S3".equals(serviceName)) {
            // Logic to get a list of S3 buckets
            serviceResponse.setBuckets(getS3Buckets());
            return serviceResponse;
        } else if ("EC2".equals(serviceName)) {
            // Logic to get a list of EC2 instance IDs
            serviceResponse.setInstances(getEC2InstanceIds());
            return serviceResponse;
        } else {
            serviceResponse.setMsg("Unexpected Service");
            return serviceResponse;
        }
    }

    @Async
    @Override
    public void findObjectsWithBucketName(String bucketName, Job job) {
        try {
            ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .build();
            ListObjectsV2Response listObjectsV2Response = s3Client.listObjectsV2(listObjectsV2Request);

            List<S3Object> contents = listObjectsV2Response.contents();

            System.out.println("Number of objects in the bucket: " + contents.stream().count());
            List<String> files = contents.stream().map(S3Object::key).toList();
            List<S3BucketFile> s3BucketFileList = new ArrayList<>();
            for (String file : files) {
                S3BucketFile s3BucketFile = new S3BucketFile(bucketName, file);
                s3BucketFileList.add(s3BucketFile);
            }

            job.setS3BucketFiles(s3BucketFileList);
            job.setS3Buckets(new ArrayList<>());
            job.setEc2Instances(new ArrayList<>());
            job.setStatus(Status.SUCCESS);
            jobRepository.save(job);

        } catch(Exception e) {
            log.info("Failed in completing the job, marking the job status to FAILED");
            job.setS3BucketFiles(new ArrayList<>());
            job.setS3Buckets(new ArrayList<>());
            job.setEc2Instances(new ArrayList<>());
            job.setStatus(Status.FAILED);
            jobRepository.save(job);
        }
    }

    @Override
    public Integer findObjectsCountWithBucketName(String bucketName, String pattern) {
        try {
            ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .build();
            ListObjectsV2Response listObjectsV2Response = s3Client.listObjectsV2(listObjectsV2Request);

            List<S3Object> contents = listObjectsV2Response.contents();
            if (Objects.nonNull(pattern)) {
                List<String> files = contents.stream().map(S3Object::key).filter(s -> s.contains(pattern)).toList();
                System.out.println("Number of objects in the bucket: " + files.size());
                files.forEach(System.out::println);
                return files.size();
            }
            System.out.println("Number of objects in the bucket: " + contents.stream().count());
            contents.stream().forEach(System.out::println);
            return contents.size();
        } catch(Exception e) {
            throw new GlobalExceptionHandler(e.getMessage());
        }
    }

    @Override
    public ServiceResponse findObjectsWithBucketNamePattern(String bucketName, String pattern, ServiceResponse serviceResponse) {
        try {
            ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .build();
            ListObjectsV2Response listObjectsV2Response = s3Client.listObjectsV2(listObjectsV2Request);

            List<S3Object> contents = listObjectsV2Response.contents();
            List<String> files = contents.stream().map(S3Object::key).filter(s -> s.contains(pattern)).collect(Collectors.toList());
            serviceResponse.setFiles(files);
            System.out.println("Number of objects in the bucket: " + contents.stream().count());
            files.stream().forEach(System.out::println);
            return serviceResponse;
        } catch(Exception e) {
            serviceResponse.setMsg(e.getMessage());
            return serviceResponse;
        }
    }

    private List<String> getS3Buckets() {
        try {
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
        } catch(Exception e) {
            throw new GlobalExceptionHandler(e.getMessage());
        }
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
        }  catch (Exception e) {
            throw new GlobalExceptionHandler(e.getMessage());
        }
    }

}
