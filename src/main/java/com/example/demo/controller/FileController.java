package com.example.demo.controller;

import com.example.demo.dao.JobRepository;
import com.example.demo.entity.Job;
import com.example.demo.entity.Status;
import com.example.demo.exceptionHandler.GlobalExceptionHandler;
import com.example.demo.request.ServiceRequest;
import com.example.demo.response.ServiceResponse;
import com.example.demo.services.impl.FileService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/")
@Slf4j
public class FileController {

    @Autowired
    FileService fileService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JobRepository jobRepository;

    private JSONParser jsonParser;

    public FileController() {
        jsonParser = new JSONParser();
    }

    @PostMapping("/services")
    public Object getServices(@RequestBody ServiceRequest serviceRequest) {
        try {
            Job job = new Job();
            job.setStatus(Status.IN_PROGRESS);
            job = jobRepository.save(job);
            fileService.getService(serviceRequest, job);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            return ResponseEntity
                    .ok()
                    .body(new ServiceResponse(job.getJobId(), job.getStatus()));
        } catch(Exception e) {
            return ResponseEntity
                    .ok()
                    .body(new ServiceResponse(e.getMessage()));
        }
    }

    /**
     * Get bucket objects count for the given bucketName and pattern(optional).
     *
     * @param jobId    Long
     * @return returns responseEntity.
     */
    @GetMapping("getJobStatus/{jobId}")
    public ResponseEntity<Object> getJobStatus(@PathVariable Long jobId) {
        try {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            ServiceResponse serviceResponse = new ServiceResponse(fileService.getJobStatusById(jobId));
            return ResponseEntity
                    .ok()
                    .body(serviceResponse);
        } catch(Exception e) {
            return ResponseEntity
                    .ok()
                    .body(new ServiceResponse(e.getMessage()));
        }
    }

    /**
     * Get bucket objects count for the given bucketName and pattern(optional).
     *
     * @param service    String
     * @return returns responseEntity.
     */
    @GetMapping("getDiscoveryResult/{service}")
    public ResponseEntity<Object> getDiscorveryResult(@PathVariable String service) {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        return ResponseEntity
                .ok()
                .body(fileService.findServiceByName(service));

    }

    /**
     * Persist bucket objects for the given bucketName.
     *
     * @param bucketName    String
     * @return returns responseEntity.
     */
    @GetMapping(value = {"getS3BucketObjects/{bucketName}"})
    public ResponseEntity<Object> persistS3BucketObjects(@PathVariable String bucketName) {
        try {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            Job job = new Job();
            job.setStatus(Status.IN_PROGRESS);
            job = jobRepository.save(job);

            fileService.findObjectsWithBucketName(bucketName, job);
            return ResponseEntity
                    .ok()
                    .body(new ServiceResponse(job.getJobId(), job.getStatus()));
        } catch(Exception e) {
            return ResponseEntity
                    .ok()
                    .body(new ServiceResponse(e.getMessage()));
        }
    }

    /**
     * Get bucket objects count for the given bucketName and pattern(optional).
     *
     * @param bucketName    String
     * @param pattern       String
     * @return returns responseEntity.
     */
    @GetMapping(value = {"getS3BucketObjects/count/{bucketName}", "getS3BucketObjects/count/{bucketName}/{pattern}"})
    public ResponseEntity<Object> getS3BucketObjectsCount(@PathVariable String bucketName,
                                                          @PathVariable(required = false) String pattern) {
        try {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            return ResponseEntity
                    .ok()
                    .body(new ServiceResponse(fileService.findObjectsCountWithBucketName(bucketName, pattern)));
        } catch(Exception e) {
            return ResponseEntity
                    .ok()
                    .body(new ServiceResponse(e.getMessage()));
        }

    }

    /**
     * Get bucket objects for the given bucketName and pattern.
     *
     * @param bucketName    String
     * @param pattern       String
     * @return returns responseEntity.
     */
    @GetMapping(value = {"getS3BucketObjects/{bucketName}/{pattern}"})
    public ResponseEntity<Object> getFiles(@PathVariable String bucketName,
                                           @PathVariable String pattern) {
        try {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            ServiceResponse serviceResponse = new ServiceResponse();
            return ResponseEntity
                    .ok()
                    .body(fileService.findObjectsWithBucketNamePattern(bucketName, pattern, serviceResponse));
        } catch(Exception e) {
            return ResponseEntity
                    .ok()
                    .body(new ServiceResponse(e.getMessage()));
        }

    }

}
