package com.hms.hospital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "billing")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Billing {

    @Id
    private String id;

    /** Patient SSN. */
    private Long patient;

    private Integer stayId;

    @Lob
    @Convert(converter = BillingItemsConverter.class)
    @Column(length = 4000)
    private List<BillingItem> items;

    private Double total;

    private Boolean paid;
}
