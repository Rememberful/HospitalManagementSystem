package com.hms.hospital.service.impl;

import com.hms.hospital.dto.request.SalaryUpsertRequest;
import com.hms.hospital.entity.Salary;
import com.hms.hospital.exception.BadRequestException;
import com.hms.hospital.repository.PhysicianRepository;
import com.hms.hospital.repository.SalaryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalaryServiceImplTest {

    @Mock
    private SalaryRepository salaryRepository;
    @Mock
    private PhysicianRepository physicianRepository;

    @InjectMocks
    private SalaryServiceImpl salaryService;

    @Test
    @DisplayName("upsert saves the salary when the physician exists (create or overwrite either way)")
    void upsertsSalary() {
        SalaryUpsertRequest request = new SalaryUpsertRequest(1, 9500.0, 0.04);
        when(physicianRepository.existsById(1)).thenReturn(true);
        when(salaryRepository.save(any(Salary.class))).thenAnswer(inv -> inv.getArgument(0));

        Salary result = salaryService.upsert(request);

        assertThat(result.getBaseSalary()).isEqualTo(9500.0);
        assertThat(result.getCommissionRate()).isEqualTo(0.04);
    }

    @Test
    @DisplayName("upsert rejects a physician reference that doesn't exist")
    void rejectsUnknownPhysician() {
        SalaryUpsertRequest request = new SalaryUpsertRequest(999, 9500.0, 0.04);
        when(physicianRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> salaryService.upsert(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("must reference an existing Physician");
    }
}
