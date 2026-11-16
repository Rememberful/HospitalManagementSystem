package com.hms.hospital.controller;

import com.hms.hospital.entity.Department;
import com.hms.hospital.entity.Physician;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.service.DepartmentService;
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

@ControllerTest(DepartmentController.class)
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DepartmentService departmentService;

    private final Department department = new Department(1, "Internal Medicine", 1);
    private final Physician head = new Physician(1, "John Dorian", "Staff Internist", 111111111L);

    @Test
    void postCreatesDepartmentAndReturns201() throws Exception {
        when(departmentService.create(any())).thenReturn(department);

        mockMvc.perform(post("/api/department")
                        .contentType("application/json")
                        .content("{\"DepartmentID\":1,\"Name\":\"Internal Medicine\",\"Head\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Record Created Successfully"));
    }

    @Test
    void postRejectsUnknownHeadWith400() throws Exception {
        when(departmentService.create(any())).thenThrow(new BadRequestException("Head must reference an existing Physician"));

        mockMvc.perform(post("/api/department")
                        .contentType("application/json")
                        .content("{\"DepartmentID\":1,\"Name\":\"Internal Medicine\",\"Head\":999}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listReturnsAllDepartments() throws Exception {
        when(departmentService.findAll()).thenReturn(List.of(department));

        mockMvc.perform(get("/api/department/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].DepartmentID").value(1));
    }

    @Test
    void getByIdReturns200() throws Exception {
        when(departmentService.getById(1)).thenReturn(department);

        mockMvc.perform(get("/api/department/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Name").value("Internal Medicine"));
    }

    @Test
    void getByIdMissingReturns404() throws Exception {
        when(departmentService.getById(99)).thenThrow(new ResourceNotFoundException("Department"));

        mockMvc.perform(get("/api/department/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getHeadReturnsPhysician() throws Exception {
        when(departmentService.getHead(1)).thenReturn(head);

        mockMvc.perform(get("/api/department/head/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.EmployeeID").value(1));
    }

    @Test
    void checkReturnsBooleanTrue() throws Exception {
        when(departmentService.isPhysicianAHead(1)).thenReturn(true);

        mockMvc.perform(get("/api/department/check/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void checkReturnsBooleanFalse() throws Exception {
        when(departmentService.isPhysicianAHead(2)).thenReturn(false);

        mockMvc.perform(get("/api/department/check/2"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void updateHeadReturns200() throws Exception {
        Department updated = new Department(1, "Internal Medicine", 2);
        when(departmentService.updateHead(eq(1), eq(2))).thenReturn(updated);

        mockMvc.perform(put("/api/department/update/headid/1")
                        .contentType("application/json")
                        .content("{\"Head\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Head").value(2));
    }

    @Test
    void updateHeadWithoutHeadFieldReturns400() throws Exception {
        mockMvc.perform(put("/api/department/update/headid/1")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
