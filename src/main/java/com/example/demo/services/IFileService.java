package com.example.demo.services;

import com.example.demo.entity.Job;
import com.example.demo.entity.Status;
import com.example.demo.request.ServiceRequest;
import com.example.demo.response.ServiceResponse;

import java.util.List;

public interface IFileService {

    /**
     * Get List of EC2Instance objects fetched using the EC2 serviceName.
     *
     * @param serviceName    String
     */
    void getService(ServiceRequest serviceName, Job job);

    /**
     * Get List of EC2Instance objects fetched using the EC2 serviceName.
     *
     * @param jobId    Long
     * @return returns Status.
     */
    Status getJobStatusById(Long jobId);

    /**
     * Get S3/EC2 objects for the given serviceName.
     *
     * @param serviceName    String
     * @return returns ServiceResponse.
     */
    ServiceResponse findServiceByName(String serviceName);

    /**
     * Get bucket objects for the given bucketName and pattern.
     *
     * @param bucketName    String
     * @return returns Object.
     */
    void findObjectsWithBucketName(String bucketName, Job job);

    /**
     * Get bucket objects count for the given bucketName and pattern.
     *
     * @param bucketName    String
     * @param pattern       String
     * @return returns Integer.
     */
    Integer findObjectsCountWithBucketName(String bucketName, String pattern);

    /**
     * Get bucket objects count for the given bucketName and pattern.
     *
     * @param bucketName    String
     * @param pattern       String
     * @return returns ServiceResponse.
     */
    ServiceResponse findObjectsWithBucketNamePattern(String bucketName, String pattern, ServiceResponse serviceResponse);
}
