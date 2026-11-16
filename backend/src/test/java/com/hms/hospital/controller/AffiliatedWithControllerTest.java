package com.hms.hospital.controller;

import com.hms.hospital.entity.AffiliatedWith;
import com.hms.hospital.entity.Department;
import com.hms.hospital.entity.Physician;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.service.AffiliatedWithService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ControllerTest(AffiliatedWithController.class)
class AffiliatedWithControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AffiliatedWithService affiliatedWithService;

    private final Physician physician = new Physician(1, "John Dorian", "Staff Internist", 111111111L);
    private final Department department = new Department(2, "Internal Medicine", 1);

    @Test
    void postCreatesLinkAndReturns201() throws Exception {
        when(affiliatedWithService.create(eq(1), eq(2), eq(true)))
                .thenReturn(new AffiliatedWith("1-2", 1, 2, 1));

        mockMvc.perform(post("/api/affiliated_with/post")
                        .contentType("application/json")
                        .content("{\"Physician\":1,\"Department\":2,\"PrimaryAffiliation\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Record Created Successfully"));
    }

    @Test
    void postRejectsUnknownPhysicianWith400() throws Exception {
        when(affiliatedWithService.create(eq(999), eq(2), any()))
                .thenThrow(new BadRequestException("Physician must reference an existing Physician"));

        mockMvc.perform(post("/api/affiliated_with/post")
                        .contentType("application/json")
                        .content("{\"Physician\":999,\"Department\":2}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void physiciansForDepartmentReturnsList() throws Exception {
        when(affiliatedWithService.getPhysiciansForDepartment(2)).thenReturn(List.of(physician));

        mockMvc.perform(get("/api/affiliated_with/physicians/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].EmployeeID").value(1));
    }

    @Test
    void departmentsForPhysicianReturnsList() throws Exception {
        when(affiliatedWithService.getDepartmentsForPhysician(1)).thenReturn(List.of(department));

        mockMvc.perform(get("/api/affiliated_with/department/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].DepartmentID").value(2));
    }

    @Test
    void primaryAffiliationReturnsDepartment() throws Exception {
        when(affiliatedWithService.getPrimaryAffiliation(1)).thenReturn(department);

        mockMvc.perform(get("/api/affiliated_with/primary/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Name").value("Internal Medicine"));
    }

    @Test
    void primaryAffiliationMissingReturns404() throws Exception {
        when(affiliatedWithService.getPrimaryAffiliation(1)).thenThrow(new ResourceNotFoundException("Primary affiliation"));

        mockMvc.perform(get("/api/affiliated_with/primary/1"))
                .andExpect(status().isNotFound());
    }
}
