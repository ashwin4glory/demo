package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@Table(uniqueConstraints =
        {@UniqueConstraint(name = "UniqueBucketAndFile", columnNames = { "bucketName", "fileName" })})
public class S3BucketFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bucketName;


    private String fileName;

    public S3BucketFile() {

    }

    public S3BucketFile(String bucketName, String file) {
        this.bucketName = bucketName;
        this.fileName = file;
    }
}
