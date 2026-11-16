package com.hms.hospital.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "prescribes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Prescription {

    /** Composite natural key "{Physician}-{Patient}-{Medication}-{Date}". */
    @Id
    private String id;

    @JsonProperty("Physician")
    private Integer physician;

    @JsonProperty("Patient")
    private Long patient;

    @JsonProperty("Medication")
    private Integer medication;

    @JsonProperty("Date")
    private String date;

    @JsonProperty("Appointment")
    private Long appointment;

    @JsonProperty("Dose")
    private String dose;
}
