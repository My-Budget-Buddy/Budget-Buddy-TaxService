package com.skillstorm.taxservice.models;

import org.junit.jupiter.api.Test;
import org.meanbean.test.BeanVerifier;

public class MeanBeanModelsTest {

    @Test
    public void testModelsGettersAndSetters() {
        // test getters and setters
        BeanVerifier.verifyBeans(
            CapitalGainsTax.class,
            Deduction.class
        );
    }
}