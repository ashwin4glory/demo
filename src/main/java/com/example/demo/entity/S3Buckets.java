package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
public class S3Buckets {

    @Id
    private Long s3Id;

    private String bucketName;

    @Lob
    private String description;

}
