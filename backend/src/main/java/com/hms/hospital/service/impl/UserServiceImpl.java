package com.hms.hospital.service.impl;

import com.hms.hospital.dto.request.UserCreateRequest;
import com.hms.hospital.dto.response.UserResponse;
import com.hms.hospital.entity.AppUser;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.repository.AppUserRepository;
import com.hms.hospital.repository.NurseRepository;
import com.hms.hospital.repository.PatientRepository;
import com.hms.hospital.repository.PhysicianRepository;
import com.hms.hospital.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Set<String> VALID_ROLES = Set.of("admin", "doctor", "nurse", "patient");

    private final AppUserRepository appUserRepository;
    private final PhysicianRepository physicianRepository;
    private final NurseRepository nurseRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse create(UserCreateRequest request) {
        String role = request.role().toLowerCase();
        if (!VALID_ROLES.contains(role)) {
            throw new BadRequestException("role must be one of admin, doctor, nurse, patient");
        }
        if (appUserRepository.existsById(request.id())) {
            throw new BadRequestException("A user with this id already exists");
        }
        if (appUserRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new BadRequestException("This username is already taken");
        }

        switch (role) {
            case "doctor" -> {
                if (request.employeeId() == null || !physicianRepository.existsById(request.employeeId())) {
                    throw new BadRequestException("employeeId must reference an existing Physician for a doctor account");
                }
            }
            case "nurse" -> {
                if (request.employeeId() == null || !nurseRepository.existsById(request.employeeId())) {
                    throw new BadRequestException("employeeId must reference an existing Nurse for a nurse account");
                }
            }
            case "patient" -> {
                if (request.ssn() == null || !patientRepository.existsById(request.ssn())) {
                    throw new BadRequestException("ssn must reference an existing Patient for a patient account");
                }
            }
            default -> { /* admin needs neither */ }
        }

        AppUser user = new AppUser(request.id(), request.username(), passwordEncoder.encode(request.password()),
                role, request.displayName(), request.ssn(), request.employeeId());
        return UserResponse.from(appUserRepository.save(user));
    }

    @Override
    public List<UserResponse> findAll() {
        return appUserRepository.findAll().stream().map(UserResponse::from).toList();
    }
}
