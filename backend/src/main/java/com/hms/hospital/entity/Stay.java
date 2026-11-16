package com.hms.hospital.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stays")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stay {

    @Id
    @JsonProperty("StayID")
    private Integer stayId;

    @JsonProperty("Patient")
    private Long patient;

    /** Room number. */
    @JsonProperty("Room")
    private Integer room;

    @JsonProperty("StayStart")
    private String stayStart;

    @JsonProperty("StayEnd")
    private String stayEnd;
}
