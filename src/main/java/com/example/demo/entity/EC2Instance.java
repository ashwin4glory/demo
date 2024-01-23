package com.example.demo.entity;


import com.amazonaws.services.ec2.model.Instance;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
public class EC2Instance extends Instance {

    @Id
    private String instanceId;

    private String instanceType;

    @Lob
    private String description;
}
