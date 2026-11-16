package com.hms.hospital.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Named AppUser (table app_users) because USER is a reserved word in several SQL dialects.
 * JSON field names stay lowercase to match the original mock API / React client exactly.
 */
@Entity
@Table(name = "app_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppUser {

    @Id
    private Integer id;

    private String username;

    private String password;

    /** admin | doctor | nurse | patient */
    private String role;

    private String displayName;

    /** Present only for patient accounts — links the login to a Patient SSN. */
    private Long ssn;

    /** Present only for doctor/nurse accounts — links the login to an EmployeeID. */
    private Integer employeeId;
}
