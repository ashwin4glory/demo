package com.example.demo.dao;

import com.example.demo.entity.S3BucketFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface S3BucketFilesRepository extends JpaRepository<S3BucketFile, Long> {

}
