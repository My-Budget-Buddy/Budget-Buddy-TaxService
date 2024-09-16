package com.skillstorm.taxservice.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

public class TestLoggingAspect {

    private LoggingAspect loggingAspect;

    @Mock
    private ProceedingJoinPoint mockProceedingJoinPoint;

    @Mock
    private Signature mockSignature;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        loggingAspect = new LoggingAspect();
    }

    @Test
    public void testLogSuccess() throws Throwable {
        // Mocking method signature and arguments
        when(mockProceedingJoinPoint.getTarget()).thenReturn(this);
        when(mockProceedingJoinPoint.getSignature()).thenReturn(mockSignature);
        when(mockSignature.getName()).thenReturn("mockMethod");
        when(mockProceedingJoinPoint.getArgs()).thenReturn(new Object[]{"arg1", "arg2"});
        when(mockProceedingJoinPoint.proceed()).thenReturn("mockResult");

        // Invoke the log method of the LoggingAspect
        Object result = loggingAspect.log(mockProceedingJoinPoint);

        // Verify that the logging behavior happens as expected
        assertEquals("mockResult", result);
        verify(mockProceedingJoinPoint, times(1)).proceed();
    }

    @Test
    public void testLogException() throws Throwable {
        // Mocking method signature and arguments
        when(mockProceedingJoinPoint.getTarget()).thenReturn(this);
        when(mockProceedingJoinPoint.getSignature()).thenReturn(mockSignature);
        when(mockSignature.getName()).thenReturn("mockMethod");
        when(mockProceedingJoinPoint.getArgs()).thenReturn(new Object[]{"arg1", "arg2"});

        // Simulate an exception being thrown during method execution
        Throwable mockThrowable = new RuntimeException("Test Exception");
        when(mockProceedingJoinPoint.proceed()).thenThrow(mockThrowable);

        // Expect the same exception to be thrown by the aspect
        assertThrows(RuntimeException.class, () -> {
            loggingAspect.log(mockProceedingJoinPoint);
        });

        // Verify that the error logging behavior happens
        verify(mockProceedingJoinPoint, times(1)).proceed();
    }
}
