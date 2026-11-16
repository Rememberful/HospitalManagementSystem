package com.hms.hospital.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "appointments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    @Id
    @JsonProperty("AppointmentID")
    private Long appointmentId;

    /** Patient SSN. */
    @JsonProperty("Patient")
    private Long patient;

    /** Nurse EmployeeID auto-assigned to prep the patient. */
    @JsonProperty("PrepNurse")
    private Integer prepNurse;

    /** Physician EmployeeID. */
    @JsonProperty("Physician")
    private Integer physician;

    @JsonProperty("Starto")
    private String starto;

    @JsonProperty("Endo")
    private String endo;

    @JsonProperty("ExaminationRoom")
    private String examinationRoom;

    @JsonProperty("VisitType")
    private String visitType;
}
