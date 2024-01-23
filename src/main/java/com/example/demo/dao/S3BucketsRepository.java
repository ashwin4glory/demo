package com.example.demo.dao;

import com.example.demo.entity.S3Buckets;
import org.springframework.data.jpa.repository.JpaRepository;

public interface S3BucketsRepository extends JpaRepository<S3Buckets, Long> {

}
