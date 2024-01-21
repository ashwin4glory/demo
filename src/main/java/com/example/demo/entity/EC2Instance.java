package com.example.demo.entity;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EC2Instance {

    private Long id;

    private String instanceId;
    private String instanceType;
}
