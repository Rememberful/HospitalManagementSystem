package com.hms.hospital.service.impl;

import com.hms.hospital.dto.request.RoomCreateRequest;
import com.hms.hospital.entity.Room;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.repository.RoomRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomServiceImplTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomServiceImpl roomService;

    @Test
    @DisplayName("create defaults Unavailable to 0 when the caller omits it")
    void createsRoomDefaultingAvailability() {
        RoomCreateRequest request = new RoomCreateRequest(101, "Single", 1, 1, null);
        when(roomRepository.existsById(101)).thenReturn(false);
        when(roomRepository.save(any(Room.class))).thenAnswer(inv -> inv.getArgument(0));

        Room result = roomService.create(request);

        assertThat(result.getUnavailable()).isEqualTo(0);
    }

    @Test
    @DisplayName("create rejects a duplicate RoomNumber")
    void rejectsDuplicateRoomNumber() {
        RoomCreateRequest request = new RoomCreateRequest(101, "Single", 1, 1, 0);
        when(roomRepository.existsById(101)).thenReturn(true);

        assertThatThrownBy(() -> roomService.create(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("updateAvailability toggles the flag on the existing room")
    void updateAvailability() {
        Room room = new Room(101, "Single", 1, 1, 0);
        when(roomRepository.findById(101)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenAnswer(inv -> inv.getArgument(0));

        Room result = roomService.updateAvailability(101, 1);

        assertThat(result.getUnavailable()).isEqualTo(1);
    }

    @Test
    @DisplayName("updateAvailability throws ResourceNotFoundException for an unknown room")
    void updateAvailabilityMissing() {
        when(roomRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.updateAvailability(999, 1))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("findAll returns every room on file")
    void findAll() {
        Room room = new Room(101, "Single", 1, 1, 0);
        when(roomRepository.findAll()).thenReturn(List.of(room));

        assertThat(roomService.findAll()).containsExactly(room);
    }

    @Test
    @DisplayName("getByRoomNumber returns the room when found")
    void getByRoomNumberFound() {
        Room room = new Room(101, "Single", 1, 1, 0);
        when(roomRepository.findById(101)).thenReturn(Optional.of(room));

        assertThat(roomService.getByRoomNumber(101)).isEqualTo(room);
    }

    @Test
    @DisplayName("getByRoomNumber throws ResourceNotFoundException for an unknown room")
    void getByRoomNumberMissing() {
        when(roomRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.getByRoomNumber(999))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
