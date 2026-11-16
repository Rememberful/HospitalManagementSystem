package com.hms.hospital.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "physicians")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Physician {

    @Id
    @JsonProperty("EmployeeID")
    private Integer employeeId;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Position")
    private String position;

    @JsonProperty("SSN")
    private Long ssn;
}
