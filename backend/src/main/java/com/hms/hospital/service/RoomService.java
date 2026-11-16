package com.hms.hospital.service;

import com.hms.hospital.dto.request.RoomCreateRequest;
import com.hms.hospital.entity.Room;

import java.util.List;

public interface RoomService {
    Room create(RoomCreateRequest request);
    Room updateAvailability(Integer roomNumber, Integer unavailable);
    List<Room> findAll();
    Room getByRoomNumber(Integer roomNumber);
}
