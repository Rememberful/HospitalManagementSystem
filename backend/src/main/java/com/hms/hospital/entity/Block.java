package com.hms.hospital.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "blocks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Block {

    /** Composite natural key "{BlockFloor}-{BlockCode}". */
    @Id
    private String id;

    @JsonProperty("BlockFloor")
    private Integer blockFloor;

    @JsonProperty("BlockCode")
    private Integer blockCode;
}
