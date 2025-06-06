package com.knowledge.manager;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class Employee {
    int id;
    String firstName;
    String lastName;
    double salary;
    Integer managerId;
}

