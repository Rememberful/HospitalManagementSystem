package com.hms.hospital.controller;

import com.hms.hospital.dto.request.RoomCreateRequest;
import com.hms.hospital.dto.response.MessageResponse;
import com.hms.hospital.entity.Room;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/room")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse create(@Valid @RequestBody RoomCreateRequest request) {
        roomService.create(request);
        return new MessageResponse("Record Created Successfully");
    }

    @GetMapping
    public List<Room> list() {
        return roomService.findAll();
    }

    @GetMapping("/{roomNumber}")
    public Room get(@PathVariable Integer roomNumber) {
        return roomService.getByRoomNumber(roomNumber);
    }

    @PutMapping("/availability/{roomNumber}")
    public Room updateAvailability(@PathVariable Integer roomNumber, @RequestBody Map<String, Integer> body) {
        Integer unavailable = body.get("Unavailable");
        if (unavailable == null) throw new BadRequestException("Unavailable is required");
        return roomService.updateAvailability(roomNumber, unavailable);
    }
}
