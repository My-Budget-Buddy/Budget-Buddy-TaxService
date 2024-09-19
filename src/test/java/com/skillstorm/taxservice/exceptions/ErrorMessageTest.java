package com.skillstorm.taxservice.exceptions;

import org.junit.jupiter.api.Test;
import org.meanbean.test.BeanVerifier;

public class ErrorMessageTest {
  
    @Test
    public void testErrorMessageGettersAndSetters() {
        BeanVerifier.verifyBean(ErrorMessage.class);
    }
}
