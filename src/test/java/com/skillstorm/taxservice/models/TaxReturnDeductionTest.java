package com.skillstorm.taxservice.models;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;

public class TaxReturnDeductionTest {

    @Test
    public void testNoArgsConstructor() {
        // Given
        TaxReturnDeduction taxReturnDeduction = new TaxReturnDeduction();

        // Then
        assertThat(taxReturnDeduction).isNotNull();
    }

    @Test
    public void testSettersAndGetters() {
        // Given
        TaxReturnDeduction taxReturnDeduction = new TaxReturnDeduction();
        int id = 1;
        TaxReturn taxReturn = mock(TaxReturn.class); // avoid circular dependency
        Deduction deduction = mock(Deduction.class); // avoid circular dependency
        BigDecimal amountSpent = BigDecimal.valueOf(2000);

        // When
        taxReturnDeduction.setId(id);
        taxReturnDeduction.setTaxReturn(taxReturn);
        taxReturnDeduction.setDeduction(deduction);
        taxReturnDeduction.setAmountSpent(amountSpent);

        // Then
        assertThat(taxReturnDeduction.getId()).isEqualTo(id);
        assertThat(taxReturnDeduction.getTaxReturn()).isEqualTo(taxReturn);
        assertThat(taxReturnDeduction.getDeduction()).isEqualTo(deduction);
        assertThat(taxReturnDeduction.getAmountSpent()).isEqualTo(amountSpent);
    }

    @Test
    public void testEqualsAndHashCode() {
        // Given
        TaxReturn taxReturn = mock(TaxReturn.class);
        Deduction deduction = mock(Deduction.class);

        TaxReturnDeduction taxReturnDeduction1 = new TaxReturnDeduction();
        TaxReturnDeduction taxReturnDeduction2 = new TaxReturnDeduction();

        taxReturnDeduction1.setId(1);
        taxReturnDeduction1.setTaxReturn(taxReturn);
        taxReturnDeduction1.setDeduction(deduction);
        taxReturnDeduction1.setAmountSpent(BigDecimal.valueOf(2000));

        taxReturnDeduction2.setId(1);
        taxReturnDeduction2.setTaxReturn(taxReturn);
        taxReturnDeduction2.setDeduction(deduction);
        taxReturnDeduction2.setAmountSpent(BigDecimal.valueOf(2000));

        // Then
        assertThat(taxReturnDeduction1).isEqualTo(taxReturnDeduction2);
        assertThat(taxReturnDeduction1.hashCode()).isEqualTo(taxReturnDeduction2.hashCode());
    }

    @Test
    public void testToString() {
        // Given
        TaxReturnDeduction taxReturnDeduction = new TaxReturnDeduction();
        taxReturnDeduction.setId(1);
        taxReturnDeduction.setAmountSpent(BigDecimal.valueOf(2000));

        // When
        String deductionString = taxReturnDeduction.toString();

        // Then
        assertThat(deductionString).contains("TaxReturnDeduction");
        assertThat(deductionString).contains("id=1");
        assertThat(deductionString).contains("amountSpent=2000");
    }
}
