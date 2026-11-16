package com.hms.hospital.service.impl;

import com.hms.hospital.dto.request.NurseCreateRequest;
import com.hms.hospital.entity.Nurse;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.repository.NurseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NurseServiceImplTest {

    @Mock
    private NurseRepository nurseRepository;

    @InjectMocks
    private NurseServiceImpl nurseService;

    private Nurse nurse;

    @BeforeEach
    void setUp() {
        nurse = new Nurse(101, "Carla Espinosa", "Head Nurse", 1, 111111110L);
    }

    @Test
    @DisplayName("create saves a new nurse when the EmployeeID is free")
    void createsNurse() {
        NurseCreateRequest request = new NurseCreateRequest(101, "Carla Espinosa", "Head Nurse", 1, 111111110L);
        when(nurseRepository.existsById(101)).thenReturn(false);
        when(nurseRepository.save(any(Nurse.class))).thenReturn(nurse);

        Nurse result = nurseService.create(request);

        assertThat(result.getName()).isEqualTo("Carla Espinosa");
    }

    @Test
    @DisplayName("create rejects a duplicate EmployeeID")
    void rejectsDuplicateEmployeeId() {
        NurseCreateRequest request = new NurseCreateRequest(101, "Carla Espinosa", "Head Nurse", 1, 111111110L);
        when(nurseRepository.existsById(101)).thenReturn(true);

        assertThatThrownBy(() -> nurseService.create(request))
                .isInstanceOf(BadRequestException.class);
        verify(nurseRepository, never()).save(any());
    }

    @Test
    @DisplayName("getByEmployeeId returns the nurse when found")
    void getByEmployeeIdFound() {
        when(nurseRepository.findById(101)).thenReturn(Optional.of(nurse));

        assertThat(nurseService.getByEmployeeId(101)).isEqualTo(nurse);
    }

    @Test
    @DisplayName("getByEmployeeId throws ResourceNotFoundException when missing")
    void getByEmployeeIdMissing() {
        when(nurseRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> nurseService.getByEmployeeId(999))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getPosition returns just the position field")
    void getPosition() {
        when(nurseRepository.findById(101)).thenReturn(Optional.of(nurse));

        assertThat(nurseService.getPosition(101)).isEqualTo("Head Nurse");
    }

    @ParameterizedTest(name = "Registered={0} -> isRegistered={1}")
    @CsvSource({"1,true", "0,false"})
    @DisplayName("isRegistered treats the stored integer flag as a boolean")
    void isRegistered(int registeredFlag, boolean expected) {
        Nurse n = new Nurse(101, "Carla Espinosa", "Head Nurse", registeredFlag, 111111110L);
        when(nurseRepository.findById(101)).thenReturn(Optional.of(n));

        assertThat(nurseService.isRegistered(101)).isEqualTo(expected);
    }

    @Test
    @DisplayName("isRegistered treats a null flag as not registered")
    void isRegisteredNullFlag() {
        Nurse n = new Nurse(101, "Carla Espinosa", "Head Nurse", null, 111111110L);
        when(nurseRepository.findById(101)).thenReturn(Optional.of(n));

        assertThat(nurseService.isRegistered(101)).isFalse();
    }

    @Test
    @DisplayName("updateRegistered saves the nurse with the new flag")
    void updateRegistered() {
        when(nurseRepository.findById(101)).thenReturn(Optional.of(nurse));
        when(nurseRepository.save(any(Nurse.class))).thenAnswer(inv -> inv.getArgument(0));

        Nurse result = nurseService.updateRegistered(101, 0);

        assertThat(result.getRegistered()).isEqualTo(0);
    }

    @Test
    @DisplayName("updateSsn saves the nurse with the new SSN")
    void updateSsn() {
        when(nurseRepository.findById(101)).thenReturn(Optional.of(nurse));
        when(nurseRepository.save(any(Nurse.class))).thenAnswer(inv -> inv.getArgument(0));

        Nurse result = nurseService.updateSsn(101, 999999999L);

        assertThat(result.getSsn()).isEqualTo(999999999L);
    }

    @Test
    @DisplayName("findAll returns every nurse on file")
    void findAll() {
        when(nurseRepository.findAll()).thenReturn(List.of(nurse));

        assertThat(nurseService.findAll()).containsExactly(nurse);
    }
}
