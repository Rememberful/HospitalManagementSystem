package com.hms.hospital.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "duty_shifts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DutyShift {

    @Id
    private String id;

    /** "physician" | "nurse" */
    private String personType;

    /** EmployeeID of the physician or nurse. */
    private Integer personId;

    private String date;

    private String joining;

    private String leaving;

    private String shiftType;
}
