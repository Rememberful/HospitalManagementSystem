package com.hms.hospital.repository;

import com.hms.hospital.entity.Appointment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AppointmentRepositoryTest {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Test
    void findByPatientReturnsOnlyThatPatientsAppointments() {
        appointmentRepository.save(new Appointment(1L, 100000001L, 101, 1, "s", "e", "A", "OPD"));
        appointmentRepository.save(new Appointment(2L, 100000002L, 101, 1, "s", "e", "B", "OPD"));

        List<Appointment> result = appointmentRepository.findByPatient(100000001L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAppointmentId()).isEqualTo(1L);
    }

    @Test
    void findByPhysicianReturnsOnlyThatPhysiciansAppointments() {
        appointmentRepository.save(new Appointment(1L, 100000001L, 101, 1, "s", "e", "A", "OPD"));
        appointmentRepository.save(new Appointment(2L, 100000002L, 101, 2, "s", "e", "B", "OPD"));

        List<Appointment> result = appointmentRepository.findByPhysician(1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPhysician()).isEqualTo(1);
    }

    @Test
    void findByPrepNurseReturnsOnlyThatNursesAssignments() {
        appointmentRepository.save(new Appointment(1L, 100000001L, 101, 1, "s", "e", "A", "OPD"));
        appointmentRepository.save(new Appointment(2L, 100000002L, 102, 1, "s", "e", "B", "OPD"));

        List<Appointment> result = appointmentRepository.findByPrepNurse(101);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAppointmentId()).isEqualTo(1L);
    }

    @Test
    void findByPhysicianAndPatientReturnsTheLinkWhenItExists() {
        appointmentRepository.save(new Appointment(1L, 100000001L, 101, 1, "s", "e", "A", "OPD"));

        Optional<Appointment> result = appointmentRepository.findByPhysicianAndPatient(1, 100000001L);

        assertThat(result).isPresent();
    }

    @Test
    void findByPhysicianAndPatientReturnsEmptyWhenNoSuchLinkExists() {
        appointmentRepository.save(new Appointment(1L, 100000001L, 101, 1, "s", "e", "A", "OPD"));

        Optional<Appointment> result = appointmentRepository.findByPhysicianAndPatient(2, 100000001L);

        assertThat(result).isEmpty();
    }
}
