package com.hms.hospital.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "departments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Department {

    @Id
    @JsonProperty("DepartmentID")
    private Integer departmentId;

    @JsonProperty("Name")
    private String name;

    /** EmployeeID of the physician heading this department. */
    @JsonProperty("Head")
    private Integer head;
}
