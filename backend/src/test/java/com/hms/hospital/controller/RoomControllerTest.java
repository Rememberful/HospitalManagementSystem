package com.hms.hospital.controller;

import com.hms.hospital.entity.Room;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ControllerTest(RoomController.class)
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomService roomService;

    private final Room room = new Room(101, "Single", 1, 1, 0);

    @Test
    void createReturns201() throws Exception {
        when(roomService.create(any())).thenReturn(room);

        mockMvc.perform(post("/api/room")
                        .contentType("application/json")
                        .content("{\"RoomNumber\":101,\"RoomType\":\"Single\",\"BlockFloor\":1,\"BlockCode\":1}"))
                .andExpect(status().isCreated());
    }

    @Test
    void createWithDuplicateRoomNumberReturns400() throws Exception {
        when(roomService.create(any())).thenThrow(new BadRequestException("Room with this RoomNumber already exists"));

        mockMvc.perform(post("/api/room")
                        .contentType("application/json")
                        .content("{\"RoomNumber\":101,\"RoomType\":\"Single\",\"BlockFloor\":1,\"BlockCode\":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listReturnsAllRooms() throws Exception {
        when(roomService.findAll()).thenReturn(List.of(room));

        mockMvc.perform(get("/api/room"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].RoomNumber").value(101));
    }

    @Test
    void updateAvailabilityReturns200() throws Exception {
        Room updated = new Room(101, "Single", 1, 1, 1);
        when(roomService.updateAvailability(eq(101), eq(1))).thenReturn(updated);

        mockMvc.perform(put("/api/room/availability/101")
                        .contentType("application/json")
                        .content("{\"Unavailable\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Unavailable").value(1));
    }

    @Test
    void updateAvailabilityMissingFieldReturns400() throws Exception {
        mockMvc.perform(put("/api/room/availability/101")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
