package com.hms.hospital.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    @JsonProperty("RoomNumber")
    private Integer roomNumber;

    @JsonProperty("RoomType")
    private String roomType;

    @JsonProperty("BlockFloor")
    private Integer blockFloor;

    @JsonProperty("BlockCode")
    private Integer blockCode;

    /** 1 = unavailable, 0 = available. */
    @JsonProperty("Unavailable")
    private Integer unavailable;
}
