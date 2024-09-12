package com.skillstorm.taxservice.dtos;

import org.junit.jupiter.api.Test;
import org.meanbean.test.BeanVerifier;

public class MeanBeanDtosTest {

    @Test
    public void testDtosGettersAndSetters() {
        // test getters and setters
        BeanVerifier.verifyBeans(
            UserDataDto.class,
            DeductionDto.class,
            OtherIncomeDto.class,
            TaxReturnCreditDto.class,
            TaxReturnDeductionDto.class,
            TaxReturnDto.class,
            W2Dto.class
        );
    }
}