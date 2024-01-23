package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long jobId;

    @OneToMany(cascade=CascadeType.ALL,orphanRemoval=true)
    private List<EC2Instance> ec2Instances;

    @OneToMany(cascade=CascadeType.ALL,orphanRemoval=true)
    private List<S3Buckets> s3Buckets;

    @OneToMany(cascade=CascadeType.ALL,orphanRemoval=true)
    private List<S3BucketFile> s3BucketFiles;

    @Enumerated(EnumType.STRING)
    private Status status;

    public Job() {
    }

    public Job(Long jobId) {
        this.jobId = jobId;
    }

    public Job(List<EC2Instance> ec2Instances, List<S3Buckets> s3Buckets, Status status) {
        this.ec2Instances = ec2Instances;
        this.s3Buckets = s3Buckets;
        this.status = status;
    }

}
