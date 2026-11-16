package com.hms.hospital.service.impl;

import com.hms.hospital.dto.request.StayAdmitRequest;
import com.hms.hospital.entity.Room;
import com.hms.hospital.entity.Stay;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.repository.PatientRepository;
import com.hms.hospital.repository.RoomRepository;
import com.hms.hospital.repository.StayRepository;
import com.hms.hospital.service.StayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StayServiceImpl implements StayService {

    private final StayRepository stayRepository;
    private final PatientRepository patientRepository;
    private final RoomRepository roomRepository;

    @Override
    public Stay admit(StayAdmitRequest request) {
        if (!patientRepository.existsById(request.patient())) {
            throw new BadRequestException("patient must reference an existing Patient");
        }
        Room room = roomRepository.findById(request.room())
                .orElseThrow(() -> new BadRequestException("room must reference an existing Room"));
        if (Integer.valueOf(1).equals(room.getUnavailable())) {
            throw new BadRequestException("This room is currently unavailable");
        }
        if (stayRepository.existsById(request.stayId())) {
            throw new BadRequestException("Stay with this StayID already exists");
        }

        room.setUnavailable(1);
        roomRepository.save(room);

        Stay stay = new Stay(request.stayId(), request.patient(), request.room(), request.stayStart(), null);
        return stayRepository.save(stay);
    }

    @Override
    public Stay discharge(Integer stayId, String stayEnd) {
        Stay stay = stayRepository.findById(stayId)
                .orElseThrow(() -> new ResourceNotFoundException("Stay"));
        stay.setStayEnd(stayEnd);
        Stay saved = stayRepository.save(stay);

        roomRepository.findById(stay.getRoom()).ifPresent(room -> {
            room.setUnavailable(0);
            roomRepository.save(room);
        });

        return saved;
    }

    @Override
    public List<Stay> findAll() {
        return stayRepository.findAll();
    }
}
