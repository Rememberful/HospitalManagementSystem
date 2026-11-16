package com.hms.hospital.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Named MedicalProcedure (not Procedure) because PROCEDURE is a reserved word in several SQL
 * dialects; the table itself keeps the original "procedures" name.
 */
@Entity
@Table(name = "procedures")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalProcedure {

    @Id
    @JsonProperty("Code")
    private Integer code;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Cost")
    private Double cost;
}
