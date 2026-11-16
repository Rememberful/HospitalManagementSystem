package com.hms.hospital.service.impl;

import com.hms.hospital.dto.request.RoomCreateRequest;
import com.hms.hospital.entity.Room;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.repository.RoomRepository;
import com.hms.hospital.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    @Override
    public Room create(RoomCreateRequest request) {
        if (roomRepository.existsById(request.roomNumber())) {
            throw new BadRequestException("Room with this RoomNumber already exists");
        }
        Room room = new Room(request.roomNumber(), request.roomType(), request.blockFloor(),
                request.blockCode(), request.unavailable() == null ? 0 : request.unavailable());
        return roomRepository.save(room);
    }

    @Override
    public Room updateAvailability(Integer roomNumber, Integer unavailable) {
        Room room = roomRepository.findById(roomNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Room"));
        room.setUnavailable(unavailable);
        return roomRepository.save(room);
    }

    @Override
    public Room getByRoomNumber(Integer roomNumber) {
        return roomRepository.findById(roomNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Room"));
    }

    @Override
    public List<Room> findAll() {
        return roomRepository.findAll();
    }
}
