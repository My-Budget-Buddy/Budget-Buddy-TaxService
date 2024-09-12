package com.skillstorm.taxservice.models.taxcredits;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

public class DependentCareTaxCreditLimitTest {

    @Test
    public void testNoArgsConstructor() {
        // Given
        DependentCareTaxCreditLimit creditLimit = new DependentCareTaxCreditLimit();

        // Then
        assertThat(creditLimit).isNotNull();
    }

    @Test
    public void testAllArgsConstructor() {
        // Given
        int id = 1;
        int numDependents = 2;
        int creditLimitValue = 3000;
        boolean refundable = true;

        // When
        DependentCareTaxCreditLimit creditLimit = new DependentCareTaxCreditLimit(id, numDependents, creditLimitValue, refundable);

        // Then
        assertThat(creditLimit.getId()).isEqualTo(id);
        assertThat(creditLimit.getNumDependents()).isEqualTo(numDependents);
        assertThat(creditLimit.getCreditLimit()).isEqualTo(creditLimitValue);
        assertThat(creditLimit.isRefundable()).isTrue();
    }

    @Test
    public void testSettersAndGetters() {
        // Given
        DependentCareTaxCreditLimit creditLimit = new DependentCareTaxCreditLimit();
        int numDependents = 3;
        int creditLimitValue = 5000;
        boolean refundable = false;

        // When
        creditLimit.setNumDependents(numDependents);
        creditLimit.setCreditLimit(creditLimitValue);
        creditLimit.setRefundable(refundable);

        // Then
        assertThat(creditLimit.getNumDependents()).isEqualTo(numDependents);
        assertThat(creditLimit.getCreditLimit()).isEqualTo(creditLimitValue);
        assertThat(creditLimit.isRefundable()).isFalse();
    }

    @Test
    public void testEqualsAndHashCode() {
        // Given
        DependentCareTaxCreditLimit creditLimit1 = new DependentCareTaxCreditLimit(1, 2, 3000, true);
        DependentCareTaxCreditLimit creditLimit2 = new DependentCareTaxCreditLimit(1, 2, 3000, true);

        // Then
        assertThat(creditLimit1).isEqualTo(creditLimit2);
        assertThat(creditLimit1.hashCode()).isEqualTo(creditLimit2.hashCode());
    }

    @Test
    public void testToString() {
        // Given
        DependentCareTaxCreditLimit creditLimit = new DependentCareTaxCreditLimit(1, 2, 3000, true);

        // When
        String creditLimitString = creditLimit.toString();

        // Then
        assertThat(creditLimitString).contains("DependentCareTaxCreditLimit");
        assertThat(creditLimitString).contains("id=1");
        assertThat(creditLimitString).contains("numDependents=2");
        assertThat(creditLimitString).contains("creditLimit=3000");
        assertThat(creditLimitString).contains("refundable=true");
    }
}
