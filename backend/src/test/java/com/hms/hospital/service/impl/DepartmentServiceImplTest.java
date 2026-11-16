package com.hms.hospital.service.impl;

import com.hms.hospital.dto.request.DepartmentCreateRequest;
import com.hms.hospital.entity.Department;
import com.hms.hospital.entity.Physician;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.repository.DepartmentRepository;
import com.hms.hospital.repository.PhysicianRepository;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceImplTest {

    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private PhysicianRepository physicianRepository;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private Physician headPhysician;
    private Department department;

    @BeforeEach
    void setUp() {
        headPhysician = new Physician(1, "John Dorian", "Staff Internist", 111111111L);
        department = new Department(1, "Internal Medicine", 1);
    }

    @Test
    @DisplayName("create saves a department when the head references a real physician")
    void createsDepartment() {
        DepartmentCreateRequest request = new DepartmentCreateRequest(1, "Internal Medicine", 1);
        when(physicianRepository.existsById(1)).thenReturn(true);
        when(departmentRepository.existsById(1)).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenReturn(department);

        Department result = departmentService.create(request);

        assertThat(result.getName()).isEqualTo("Internal Medicine");
    }

    @Test
    @DisplayName("create rejects a Head that isn't a real physician")
    void rejectsUnknownHead() {
        DepartmentCreateRequest request = new DepartmentCreateRequest(1, "Internal Medicine", 999);
        when(physicianRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> departmentService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Head must reference");
        verify(departmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("create rejects a duplicate DepartmentID")
    void rejectsDuplicateId() {
        DepartmentCreateRequest request = new DepartmentCreateRequest(1, "Internal Medicine", 1);
        when(physicianRepository.existsById(1)).thenReturn(true);
        when(departmentRepository.existsById(1)).thenReturn(true);

        assertThatThrownBy(() -> departmentService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("getById returns the department when found")
    void getByIdFound() {
        when(departmentRepository.findById(1)).thenReturn(Optional.of(department));

        assertThat(departmentService.getById(1)).isEqualTo(department);
    }

    @Test
    @DisplayName("getById throws ResourceNotFoundException when missing")
    void getByIdMissing() {
        when(departmentRepository.findById(2)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.getById(2))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getHead resolves the department's Head to a full Physician")
    void getHead() {
        when(departmentRepository.findById(1)).thenReturn(Optional.of(department));
        when(physicianRepository.findById(1)).thenReturn(Optional.of(headPhysician));

        Physician result = departmentService.getHead(1);

        assertThat(result).isEqualTo(headPhysician);
    }

    @Test
    @DisplayName("getHead throws ResourceNotFoundException if the Head physician record is gone")
    void getHeadDanglingReference() {
        when(departmentRepository.findById(1)).thenReturn(Optional.of(department));
        when(physicianRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.getHead(1))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("isPhysicianAHead returns true when a department has that physician as Head")
    void isHeadTrue() {
        when(departmentRepository.findByHead(1)).thenReturn(Optional.of(department));

        assertThat(departmentService.isPhysicianAHead(1)).isTrue();
    }

    @Test
    @DisplayName("isPhysicianAHead returns false when no department has that physician as Head")
    void isHeadFalse() {
        when(departmentRepository.findByHead(2)).thenReturn(Optional.empty());

        assertThat(departmentService.isPhysicianAHead(2)).isFalse();
    }

    @Test
    @DisplayName("updateHead saves the department with the new Head")
    void updateHead() {
        Physician newHead = new Physician(2, "Elliot Reid", "Attending Physician", 222222222L);
        when(physicianRepository.existsById(2)).thenReturn(true);
        when(departmentRepository.findById(1)).thenReturn(Optional.of(department));
        when(departmentRepository.save(any(Department.class))).thenAnswer(inv -> inv.getArgument(0));

        Department result = departmentService.updateHead(1, 2);

        assertThat(result.getHead()).isEqualTo(2);
    }

    @Test
    @DisplayName("updateHead rejects a new Head that isn't a real physician")
    void updateHeadRejectsUnknownPhysician() {
        when(physicianRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> departmentService.updateHead(1, 999))
                .isInstanceOf(BadRequestException.class);
        verify(departmentRepository, never()).findById(any());
    }

    @Test
    @DisplayName("findAll returns every department on file")
    void findAll() {
        when(departmentRepository.findAll()).thenReturn(List.of(department));

        assertThat(departmentService.findAll()).containsExactly(department);
    }
}
