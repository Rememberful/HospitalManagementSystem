package com.hms.hospital.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "undergoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Undergoes {

    @Id
    private String id;

    @JsonProperty("Patient")
    private Long patient;

    /** Procedure code performed. */
    @JsonProperty("Procedures")
    private Integer procedures;

    @JsonProperty("Stay")
    private Integer stay;

    @JsonProperty("DateUndergoes")
    private String dateUndergoes;

    @JsonProperty("Physician")
    private Integer physician;

    /** Nurse EmployeeID assisting. */
    @JsonProperty("AssistingNurse")
    private Integer assistingNurse;
}
