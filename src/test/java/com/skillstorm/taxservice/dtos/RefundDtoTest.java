package com.skillstorm.taxservice.dtos;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

public class RefundDtoTest {

    @Test
    public void testAllArgsConstructor() {
        // Given
        BigDecimal federalRefund = BigDecimal.valueOf(1200.55);
        BigDecimal stateRefund = BigDecimal.valueOf(500.75);

        // When
        RefundDto refundDto = new RefundDto(federalRefund, stateRefund);

        // Then
        assertThat(refundDto.getFederalRefund()).isEqualTo(federalRefund);
        assertThat(refundDto.getStateRefund()).isEqualTo(stateRefund);
    }

    @Test
    public void testSettersAndGetters() {
        // Given
        RefundDto refundDto = new RefundDto(BigDecimal.ZERO, BigDecimal.ZERO);
        BigDecimal newFederalRefund = BigDecimal.valueOf(1500.55);
        BigDecimal newStateRefund = BigDecimal.valueOf(800.25);

        // When
        refundDto.setFederalRefund(newFederalRefund);
        refundDto.setStateRefund(newStateRefund);

        // Then
        assertThat(refundDto.getFederalRefund()).isEqualTo(newFederalRefund);
        assertThat(refundDto.getStateRefund()).isEqualTo(newStateRefund);
    }

    @Test
    public void testEqualsAndHashCode() {
        // Given
        BigDecimal federalRefund = BigDecimal.valueOf(1200.55);
        BigDecimal stateRefund = BigDecimal.valueOf(500.75);

        RefundDto refundDto1 = new RefundDto(federalRefund, stateRefund);
        RefundDto refundDto2 = new RefundDto(federalRefund, stateRefund);

        // Then
        assertThat(refundDto1).isEqualTo(refundDto2);
        assertThat(refundDto1.hashCode()).isEqualTo(refundDto2.hashCode());
    }

    @Test
    public void testToString() {
        // Given
        BigDecimal federalRefund = BigDecimal.valueOf(1200.55);
        BigDecimal stateRefund = BigDecimal.valueOf(500.75);
        RefundDto refundDto = new RefundDto(federalRefund, stateRefund);

        // When
        String refundDtoString = refundDto.toString();

        // Then
        assertThat(refundDtoString).contains("federalRefund=1200.55");
        assertThat(refundDtoString).contains("stateRefund=500.75");
    }
}
