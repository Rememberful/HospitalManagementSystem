package com.hms.hospital.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "trained_in")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainedIn {

    /** Composite natural key "{Physician}-{Treatment}". */
    @Id
    private String id;

    @JsonProperty("Physician")
    private Integer physician;

    /** Procedure code the physician is certified to perform. */
    @JsonProperty("Treatment")
    private Integer treatment;

    @JsonProperty("CertificationDate")
    private String certificationDate;

    @JsonProperty("CertificationExpires")
    private String certificationExpires;
}
