package com.hms.hospital.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patient {

    @Id
    @JsonProperty("SSN")
    private Long ssn;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Address")
    private String address;

    @JsonProperty("Phone")
    private String phone;

    @JsonProperty("InsuranceID")
    private Long insuranceId;

    /** EmployeeID of the primary care physician. */
    @JsonProperty("PCP")
    private Integer pcp;
}
