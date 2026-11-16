package com.hms.hospital.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "nurses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Nurse {

    @Id
    @JsonProperty("EmployeeID")
    private Integer employeeId;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Position")
    private String position;

    /** 1 = registered nurse, 0 = not. Stored as int to mirror the original data file. */
    @JsonProperty("Registered")
    private Integer registered;

    @JsonProperty("SSN")
    private Long ssn;
}
