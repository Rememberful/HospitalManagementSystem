package com.hms.hospital.service.impl;

import com.hms.hospital.entity.AffiliatedWith;
import com.hms.hospital.entity.Department;
import com.hms.hospital.entity.Physician;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.exception.ResourceNotFoundException;
import com.hms.hospital.repository.AffiliatedWithRepository;
import com.hms.hospital.repository.DepartmentRepository;
import com.hms.hospital.repository.PhysicianRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AffiliatedWithServiceImplTest {

    @Mock
    private AffiliatedWithRepository affiliatedWithRepository;
    @Mock
    private PhysicianRepository physicianRepository;
    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private AffiliatedWithServiceImpl affiliatedWithService;

    @Test
    @DisplayName("create builds the composite id from Physician and Department and stores PrimaryAffiliation as 1")
    void createsPrimaryAffiliation() {
        when(physicianRepository.existsById(1)).thenReturn(true);
        when(departmentRepository.existsById(2)).thenReturn(true);
        when(affiliatedWithRepository.existsById("1-2")).thenReturn(false);
        when(affiliatedWithRepository.save(any(AffiliatedWith.class))).thenAnswer(inv -> inv.getArgument(0));

        AffiliatedWith result = affiliatedWithService.create(1, 2, true);

        assertThat(result.getId()).isEqualTo("1-2");
        assertThat(result.getPrimaryAffiliation()).isEqualTo(1);
    }

    @Test
    @DisplayName("create stores PrimaryAffiliation as 0 when not marked primary")
    void createsNonPrimaryAffiliation() {
        when(physicianRepository.existsById(1)).thenReturn(true);
        when(departmentRepository.existsById(2)).thenReturn(true);
        when(affiliatedWithRepository.existsById("1-2")).thenReturn(false);
        when(affiliatedWithRepository.save(any(AffiliatedWith.class))).thenAnswer(inv -> inv.getArgument(0));

        AffiliatedWith result = affiliatedWithService.create(1, 2, false);

        assertThat(result.getPrimaryAffiliation()).isEqualTo(0);
    }

    @Test
    @DisplayName("create rejects a null Physician or Department")
    void rejectsNullFields() {
        assertThatThrownBy(() -> affiliatedWithService.create(null, 2, true))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> affiliatedWithService.create(1, null, true))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("create rejects an unknown Physician")
    void rejectsUnknownPhysician() {
        when(physicianRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> affiliatedWithService.create(999, 2, true))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Physician must reference");
    }

    @Test
    @DisplayName("create rejects an unknown Department")
    void rejectsUnknownDepartment() {
        when(physicianRepository.existsById(1)).thenReturn(true);
        when(departmentRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> affiliatedWithService.create(1, 999, true))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Department must reference");
    }

    @Test
    @DisplayName("create rejects a duplicate Physician/Department link")
    void rejectsDuplicateLink() {
        when(physicianRepository.existsById(1)).thenReturn(true);
        when(departmentRepository.existsById(2)).thenReturn(true);
        when(affiliatedWithRepository.existsById("1-2")).thenReturn(true);

        assertThatThrownBy(() -> affiliatedWithService.create(1, 2, true))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already affiliated");
    }

    @Test
    @DisplayName("getPhysiciansForDepartment resolves affiliation links to full Physician records")
    void getPhysiciansForDepartment() {
        Physician physician = new Physician(1, "John Dorian", "Staff Internist", 111111111L);
        AffiliatedWith link = new AffiliatedWith("1-2", 1, 2, 1);
        when(affiliatedWithRepository.findByDepartment(2)).thenReturn(List.of(link));
        when(physicianRepository.findAllById(Set.of(1))).thenReturn(List.of(physician));

        assertThat(affiliatedWithService.getPhysiciansForDepartment(2)).containsExactly(physician);
    }

    @Test
    @DisplayName("getDepartmentsForPhysician resolves affiliation links to full Department records")
    void getDepartmentsForPhysician() {
        Department department = new Department(2, "Internal Medicine", 1);
        AffiliatedWith link = new AffiliatedWith("1-2", 1, 2, 1);
        when(affiliatedWithRepository.findByPhysician(1)).thenReturn(List.of(link));
        when(departmentRepository.findAllById(Set.of(2))).thenReturn(List.of(department));

        assertThat(affiliatedWithService.getDepartmentsForPhysician(1)).containsExactly(department);
    }

    @Test
    @DisplayName("getPrimaryAffiliation returns the department marked as primary for that physician")
    void getPrimaryAffiliation() {
        Department department = new Department(2, "Internal Medicine", 1);
        AffiliatedWith link = new AffiliatedWith("1-2", 1, 2, 1);
        when(affiliatedWithRepository.findByPhysicianAndPrimaryAffiliation(1, 1)).thenReturn(Optional.of(link));
        when(departmentRepository.findById(2)).thenReturn(Optional.of(department));

        assertThat(affiliatedWithService.getPrimaryAffiliation(1)).isEqualTo(department);
    }

    @Test
    @DisplayName("getPrimaryAffiliation throws ResourceNotFoundException when the physician has no primary department")
    void getPrimaryAffiliationMissing() {
        when(affiliatedWithRepository.findByPhysicianAndPrimaryAffiliation(1, 1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> affiliatedWithService.getPrimaryAffiliation(1))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
