package com.hms.hospital.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "affiliated_with")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AffiliatedWith {

    /** Composite natural key "{Physician}-{Department}". */
    @Id
    private String id;

    @JsonProperty("Physician")
    private Integer physician;

    @JsonProperty("Department")
    private Integer department;

    @JsonProperty("PrimaryAffiliation")
    private Integer primaryAffiliation;
}
