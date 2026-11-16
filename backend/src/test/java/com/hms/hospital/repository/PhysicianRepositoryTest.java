package com.hms.hospital.repository;

import com.hms.hospital.entity.Physician;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @DataJpaTest auto-configures an embedded database for the slice, independent of whatever
 * is in application.properties, so this runs against a fresh in-memory H2 instance every time.
 */
@DataJpaTest
class PhysicianRepositoryTest {

    @Autowired
    private PhysicianRepository physicianRepository;

    @Test
    void findByNameIgnoreCaseMatchesRegardlessOfCase() {
        physicianRepository.save(new Physician(1, "John Dorian", "Staff Internist", 111111111L));

        Optional<Physician> result = physicianRepository.findByNameIgnoreCase("john dorian");

        assertThat(result).isPresent();
        assertThat(result.get().getEmployeeId()).isEqualTo(1);
    }

    @Test
    void findByNameIgnoreCaseReturnsEmptyWhenNoMatch() {
        assertThat(physicianRepository.findByNameIgnoreCase("nobody")).isEmpty();
    }

    @Test
    void findByPositionIgnoreCaseReturnsEveryMatchingPhysician() {
        physicianRepository.save(new Physician(1, "John Dorian", "Staff Internist", 111111111L));
        physicianRepository.save(new Physician(4, "Bob Kelso", "Staff Internist", 444444444L));
        physicianRepository.save(new Physician(2, "Elliot Reid", "Attending Physician", 222222222L));

        List<Physician> result = physicianRepository.findByPositionIgnoreCase("staff internist");

        assertThat(result).hasSize(2)
                .extracting(Physician::getEmployeeId)
                .containsExactlyInAnyOrder(1, 4);
    }

    @Test
    void saveAndFindByIdRoundTrips() {
        physicianRepository.save(new Physician(9, "New Physician", "Resident", 999999999L));

        Optional<Physician> found = physicianRepository.findById(9);

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("New Physician");
    }
}
