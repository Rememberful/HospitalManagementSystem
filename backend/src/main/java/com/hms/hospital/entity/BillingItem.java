package com.hms.hospital.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One line item within a Billing record (Procedure / Room / Medication charge). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillingItem {
    private String type;
    private String label;
    private Double amount;
}
