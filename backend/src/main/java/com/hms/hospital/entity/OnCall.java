package com.hms.hospital.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "on_call")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnCall {

    @Id
    private String id;

    /** Nurse EmployeeID. */
    @JsonProperty("Nurse")
    private Integer nurse;

    @JsonProperty("BlockFloor")
    private Integer blockFloor;

    @JsonProperty("BlockCode")
    private Integer blockCode;

    @JsonProperty("OnCallStart")
    private String onCallStart;

    @JsonProperty("OnCallEnd")
    private String onCallEnd;
}
