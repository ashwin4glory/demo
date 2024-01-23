package com.example.demo.dao;

import com.example.demo.entity.EC2Instance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EC2InstanceRepository extends JpaRepository<EC2Instance, Long> {

}
