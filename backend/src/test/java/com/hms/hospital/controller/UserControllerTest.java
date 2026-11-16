package com.hms.hospital.controller;

import com.hms.hospital.dto.response.UserResponse;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ControllerTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private final UserResponse doctorAccount = new UserResponse(5, "drwho", "doctor", "Dr Who", null, 1);

    @Test
    void createReturns201() throws Exception {
        when(userService.create(any())).thenReturn(doctorAccount);

        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content("{\"id\":5,\"username\":\"drwho\",\"password\":\"pw\",\"role\":\"doctor\",\"displayName\":\"Dr Who\",\"employeeId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("drwho"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void createWithInvalidRoleReturns400() throws Exception {
        when(userService.create(any())).thenThrow(new BadRequestException("role must be one of admin, doctor, nurse, patient"));

        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content("{\"id\":5,\"username\":\"x\",\"password\":\"pw\",\"role\":\"superuser\",\"displayName\":\"X\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWithMissingFieldsReturns400() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content("{\"username\":\"x\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listReturnsAllAccounts() throws Exception {
        when(userService.findAll()).thenReturn(List.of(doctorAccount));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("drwho"));
    }
}
