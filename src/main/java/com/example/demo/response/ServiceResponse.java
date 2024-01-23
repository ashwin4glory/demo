package com.example.demo.response;

import com.example.demo.entity.Status;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class ServiceResponse {

    private Long jobId;

    private String msg;

    private Integer count;

    private Status status;

    private List<String> instances;

    private List<String> buckets;

    private List<String> files;

    public ServiceResponse() {
    }

    public ServiceResponse(Long jobId) {
        this.jobId = jobId;
    }

    public ServiceResponse(String msg) {
        this.msg = msg;
    }

    public ServiceResponse(Integer count) {
        this.count = count;
    }

    public ServiceResponse(Status status) {
        this.status = status;
    }

    public ServiceResponse(Long jobId, Status status) {
        this.jobId = jobId;
        this.status = status;
    }

}
