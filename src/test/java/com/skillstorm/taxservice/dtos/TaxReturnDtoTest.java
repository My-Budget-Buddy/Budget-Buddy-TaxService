package com.skillstorm.taxservice.dtos;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.skillstorm.taxservice.constants.FilingStatus;
import com.skillstorm.taxservice.models.TaxReturn;

public class TaxReturnDtoTest {

    @Test
    public void testJsonCreatorConstructorWithFilingStatus() {
        // Given
        String filingStatusInput = "HEAD OF HOUSEHOLD";

        // When
        TaxReturnDto taxReturnDto = new TaxReturnDto(filingStatusInput);

        // Then
        assertThat(taxReturnDto.getFilingStatus()).isEqualTo(FilingStatus.HEAD_OF_HOUSEHOLD);
    }

    @Test
    public void testJsonCreatorConstructorWithFilingStatusNull() {
        // Given
        String filingStatusInput = null;

        // When
        TaxReturnDto taxReturnDto = new TaxReturnDto(filingStatusInput);

        // Then
        assertThat(taxReturnDto.getFilingStatus()).isEqualTo(FilingStatus.SINGLE); // Default value
    }

    @Test
    public void testTaxReturnDtoDOBNull() {
        // Given
        TaxReturn taxReturn = new TaxReturn();

        // When
        TaxReturnDto taxReturnDto = new TaxReturnDto(taxReturn);

        // Then
        assertThat(taxReturnDto.getDateOfBirth()).isEqualTo(null); // Default value
    }
}
