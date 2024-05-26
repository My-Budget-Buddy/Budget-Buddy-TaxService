package com.skillstorm.taxservice.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.skillstorm.taxservice.models.FilingStatus;

@DataJpaTest
public class FilingStatusRepositoryTest {

    @Autowired
    private FilingStatusRepository filingStatusRepository;

    @Test
    public void testFindByStatus() {
        // Given
        String status = "Single";

        // When
        Optional<FilingStatus> filingStatusOptional = filingStatusRepository.findByStatus(status);

        // Then
        assertThat(filingStatusOptional).isPresent();
        FilingStatus filingStatus = filingStatusOptional.get();
        assertThat(filingStatus.getStatus()).isEqualTo(status);
    }

    @Test
    public void testFindByStatus_NotFound() {
        // Given
        String status = "NonExistentStatus";

        // When
        Optional<FilingStatus> filingStatusOptional = filingStatusRepository.findByStatus(status);

        // Then
        assertThat(filingStatusOptional).isNotPresent();
    }
}
