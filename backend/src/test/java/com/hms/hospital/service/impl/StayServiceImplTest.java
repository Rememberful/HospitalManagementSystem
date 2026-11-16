package com.hms.hospital.service.impl;

import com.hms.hospital.dto.request.StayAdmitRequest;
import com.hms.hospital.entity.Room;
import com.hms.hospital.entity.Stay;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.repository.PatientRepository;
import com.hms.hospital.repository.RoomRepository;
import com.hms.hospital.repository.StayRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StayServiceImplTest {

    @Mock private StayRepository stayRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private RoomRepository roomRepository;

    @InjectMocks
    private StayServiceImpl stayService;

    @Test
    @DisplayName("admit creates the stay and marks the room unavailable")
    void admitsPatient() {
        StayAdmitRequest request = new StayAdmitRequest(3215, 100000001L, 101, "2030-01-01");
        Room room = new Room(101, "Single", 1, 1, 0);
        when(patientRepository.existsById(100000001L)).thenReturn(true);
        when(roomRepository.findById(101)).thenReturn(Optional.of(room));
        when(stayRepository.existsById(3215)).thenReturn(false);
        when(roomRepository.save(any(Room.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stayRepository.save(any(Stay.class))).thenAnswer(inv -> inv.getArgument(0));

        Stay result = stayService.admit(request);

        assertThat(result.getStayEnd()).isNull();
        verify(roomRepository).save(argThat(r -> r.getUnavailable() == 1));
    }

    @Test
    @DisplayName("admit rejects a room that is already unavailable")
    void rejectsUnavailableRoom() {
        StayAdmitRequest request = new StayAdmitRequest(3215, 100000001L, 101, "2030-01-01");
        Room room = new Room(101, "Single", 1, 1, 1);
        when(patientRepository.existsById(100000001L)).thenReturn(true);
        when(roomRepository.findById(101)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> stayService.admit(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("currently unavailable");
    }

    @Test
    @DisplayName("admit rejects an unknown patient")
    void rejectsUnknownPatient() {
        StayAdmitRequest request = new StayAdmitRequest(3215, 999L, 101, "2030-01-01");
        when(patientRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> stayService.admit(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("patient must reference");
    }

    @Test
    @DisplayName("admit rejects a duplicate StayID")
    void rejectsDuplicateStayId() {
        StayAdmitRequest request = new StayAdmitRequest(3215, 100000001L, 101, "2030-01-01");
        Room room = new Room(101, "Single", 1, 1, 0);
        when(patientRepository.existsById(100000001L)).thenReturn(true);
        when(roomRepository.findById(101)).thenReturn(Optional.of(room));
        when(stayRepository.existsById(3215)).thenReturn(true);

        assertThatThrownBy(() -> stayService.admit(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("discharge sets StayEnd and frees up the room")
    void dischargesPatientAndFreesRoom() {
        Stay stay = new Stay(3215, 100000001L, 101, "2030-01-01", null);
        Room room = new Room(101, "Single", 1, 1, 1);
        when(stayRepository.findById(3215)).thenReturn(Optional.of(stay));
        when(stayRepository.save(any(Stay.class))).thenAnswer(inv -> inv.getArgument(0));
        when(roomRepository.findById(101)).thenReturn(Optional.of(room));

        Stay result = stayService.discharge(3215, "2030-01-05");

        assertThat(result.getStayEnd()).isEqualTo("2030-01-05");
        verify(roomRepository).save(argThat(r -> r.getUnavailable() == 0));
    }

    @Test
    @DisplayName("discharge throws ResourceNotFoundException for an unknown StayID")
    void dischargeMissingStay() {
        when(stayRepository.findById(404)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stayService.discharge(404, "2030-01-05"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
