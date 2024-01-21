package com.example.demo.controller;

import com.amazonaws.services.ec2.model.Instance;
import com.example.demo.request.ServiceRequest;
import com.example.demo.services.FileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class FileController {

    private static final String MESSAGE_1 = "Uploaded the file successfully";
    private static final String FILE_NAME = "fileName";

    @Autowired
    FileService fileService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/services")
    public Object getServices(@RequestBody ServiceRequest serviceRequest) {
        List<Object> resp = new ArrayList<>();
        for (String service: serviceRequest.getServices()) {
            if (service.equals("EC2")) {
                resp.add(fileService.getEC2Service(service));
            } else if (service.equals("S3")) {
                resp.add(fileService.getS3Service(service));
            } else {
                log.warn("Unsupported service");
            }
        }
        JSONArray json = null;
        JSONParser jsonParser = new JSONParser();
        try {
            json = (JSONArray) jsonParser.parse(objectMapper.writeValueAsString(resp));
        } catch(Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity
                .ok()
                .body(json);
    }

    @GetMapping("getDiscoveryResult/{service}")
    public ResponseEntity<Object> getDiscorveryResult(@PathVariable String service) {
        return ResponseEntity
                .ok()
                .body(fileService.findServiceByName(service));

    }

    @GetMapping(value = {"getS3BucketObjects/{bucketName}", "getS3BucketObjects/{bucketName}/{pattern}"})
    public ResponseEntity<Object> getS3BucketObjects(@PathVariable String bucketName,
                                                     @PathVariable(required = false) String pattern) {
        return ResponseEntity
                .ok()
                .body(fileService.findByName(bucketName, pattern));

    }

    @GetMapping(value = {"getS3BucketObjects/count/{bucketName}", "getS3BucketObjects/count/{bucketName}/{pattern}"})
    public ResponseEntity<Object> getS3BucketObjectsCount(@PathVariable String bucketName,
                                                          @PathVariable(required = false) String pattern) {
        return ResponseEntity
                .ok()
                .body(fileService.findCountByName(bucketName, pattern));

    }

}
