package com.skillstorm.taxservice.models;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class W2Test {

    @Test
    public void testNoArgsConstructor() {
        // Given
        W2 w2 = new W2();

        // Then
        assertThat(w2).isNotNull();
    }

    @Test
    public void testSettersAndGetters() {
        // Given
        W2 w2 = new W2();
        int id = 1;
        TaxReturn taxReturn = new TaxReturn();
        int year = 2024;
        int userId = 123;
        String employer = "Employer Inc.";
        BigDecimal wages = BigDecimal.valueOf(50000);
        int state = 12;
        BigDecimal federalIncomeTaxWithheld = BigDecimal.valueOf(5000);
        BigDecimal stateIncomeTaxWithheld = BigDecimal.valueOf(1000);
        BigDecimal socialSecurityTaxWithheld = BigDecimal.valueOf(3000);
        BigDecimal medicareTaxWithheld = BigDecimal.valueOf(1500);
        String imageKey = "imageKey123";

        // When
        w2.setId(id);
        w2.setTaxReturn(taxReturn);
        w2.setYear(year);
        w2.setUserId(userId);
        w2.setEmployer(employer);
        w2.setWages(wages);
        w2.setState(state);
        w2.setFederalIncomeTaxWithheld(federalIncomeTaxWithheld);
        w2.setStateIncomeTaxWithheld(stateIncomeTaxWithheld);
        w2.setSocialSecurityTaxWithheld(socialSecurityTaxWithheld);
        w2.setMedicareTaxWithheld(medicareTaxWithheld);
        w2.setImageKey(imageKey);

        // Then
        assertThat(w2.getId()).isEqualTo(id);
        assertThat(w2.getTaxReturn()).isEqualTo(taxReturn);
        assertThat(w2.getYear()).isEqualTo(year);
        assertThat(w2.getUserId()).isEqualTo(userId);
        assertThat(w2.getEmployer()).isEqualTo(employer);
        assertThat(w2.getWages()).isEqualTo(wages);
        assertThat(w2.getState()).isEqualTo(state);
        assertThat(w2.getFederalIncomeTaxWithheld()).isEqualTo(federalIncomeTaxWithheld);
        assertThat(w2.getStateIncomeTaxWithheld()).isEqualTo(stateIncomeTaxWithheld);
        assertThat(w2.getSocialSecurityTaxWithheld()).isEqualTo(socialSecurityTaxWithheld);
        assertThat(w2.getMedicareTaxWithheld()).isEqualTo(medicareTaxWithheld);
        assertThat(w2.getImageKey()).isEqualTo(imageKey);
    }

    @Test
    public void testEqualsAndHashCode() {
        // Given
        TaxReturn taxReturn = new TaxReturn();
        W2 w2_1 = new W2();
        W2 w2_2 = new W2();

        w2_1.setId(1);
        w2_1.setTaxReturn(taxReturn);
        w2_1.setYear(2024);
        w2_1.setUserId(123);
        w2_1.setEmployer("Employer Inc.");
        w2_1.setWages(BigDecimal.valueOf(50000));
        w2_1.setState(12);
        w2_1.setFederalIncomeTaxWithheld(BigDecimal.valueOf(5000));
        w2_1.setStateIncomeTaxWithheld(BigDecimal.valueOf(1000));
        w2_1.setSocialSecurityTaxWithheld(BigDecimal.valueOf(3000));
        w2_1.setMedicareTaxWithheld(BigDecimal.valueOf(1500));
        w2_1.setImageKey("imageKey123");

        w2_2.setId(1);
        w2_2.setTaxReturn(taxReturn);
        w2_2.setYear(2024);
        w2_2.setUserId(123);
        w2_2.setEmployer("Employer Inc.");
        w2_2.setWages(BigDecimal.valueOf(50000));
        w2_2.setState(12);
        w2_2.setFederalIncomeTaxWithheld(BigDecimal.valueOf(5000));
        w2_2.setStateIncomeTaxWithheld(BigDecimal.valueOf(1000));
        w2_2.setSocialSecurityTaxWithheld(BigDecimal.valueOf(3000));
        w2_2.setMedicareTaxWithheld(BigDecimal.valueOf(1500));
        w2_2.setImageKey("imageKey123");

        // Then
        assertThat(w2_1).isEqualTo(w2_2);
        assertThat(w2_1.hashCode()).isEqualTo(w2_2.hashCode());
    }

    @Test
    public void testToString() {
        // Given
        TaxReturn taxReturn = mock(TaxReturn.class); // avoid circular dependencies
        when(taxReturn.getId()).thenReturn(1);

        W2 w2 = new W2();
        w2.setId(1);
        w2.setTaxReturn(taxReturn);
        w2.setYear(2024);
        w2.setUserId(123);
        w2.setEmployer("Employer Inc.");
        w2.setWages(BigDecimal.valueOf(50000));
        w2.setState(12);
        w2.setFederalIncomeTaxWithheld(BigDecimal.valueOf(5000));
        w2.setStateIncomeTaxWithheld(BigDecimal.valueOf(1000));
        w2.setSocialSecurityTaxWithheld(BigDecimal.valueOf(3000));
        w2.setMedicareTaxWithheld(BigDecimal.valueOf(1500));
        w2.setImageKey("imageKey123");

        // When
        String w2String = w2.toString();

        // Then
        assertThat(w2String).contains("W2");
        assertThat(w2String).contains("id=1");
        assertThat(w2String).contains("taxReturn=1");
        assertThat(w2String).contains("year=2024");
        assertThat(w2String).contains("userId=123");
        assertThat(w2String).contains("employer='Employer Inc.'");
        assertThat(w2String).contains("wages=50000");
        assertThat(w2String).contains("state=12");
        assertThat(w2String).contains("federalIncomeTaxWithheld=5000");
        assertThat(w2String).contains("stateIncomeTaxWithheld=1000");
        assertThat(w2String).contains("socialSecurityTaxWithheld=3000");
        assertThat(w2String).contains("medicareTaxWithheld=1500");
        assertThat(w2String).contains("imageKey='imageKey123'");
    }
}
