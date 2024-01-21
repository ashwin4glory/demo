package com.example.demo.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class S3Buckets {
    private Long id;

    private String bucketName;
    private String region;

}
