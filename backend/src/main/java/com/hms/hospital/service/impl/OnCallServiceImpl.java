package com.hms.hospital.service.impl;

import com.hms.hospital.dto.request.OnCallCreateRequest;
import com.hms.hospital.entity.OnCall;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.repository.NurseRepository;
import com.hms.hospital.repository.OnCallRepository;
import com.hms.hospital.service.OnCallService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OnCallServiceImpl implements OnCallService {

    private final OnCallRepository onCallRepository;
    private final NurseRepository nurseRepository;

    @Override
    public OnCall create(OnCallCreateRequest request) {
        if (!nurseRepository.existsById(request.nurse())) {
            throw new BadRequestException("nurse must reference an existing Nurse");
        }
        OnCall onCall = new OnCall(UUID.randomUUID().toString(), request.nurse(), request.blockFloor(),
                request.blockCode(), request.onCallStart(), request.onCallEnd());
        return onCallRepository.save(onCall);
    }

    @Override
    public List<OnCall> findAll() {
        return onCallRepository.findAll();
    }

    @Override
    public void delete(String id) {
        if (!onCallRepository.existsById(id)) {
            throw new ResourceNotFoundException("OnCall");
        }
        onCallRepository.deleteById(id);
    }
}
