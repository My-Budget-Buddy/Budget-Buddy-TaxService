package com.skillstorm.taxservice.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    // Pointcut for all methods in the com.skillstorm package:
    @Pointcut("within(com.skillstorm..*)")
    public void everything() {
        /* Empty Hook */
    }

    @Around("everything()")
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public Object log(ProceedingJoinPoint pjp) throws Throwable {

        Object result = null;

        // Define our logger to log method calls and their signatures:
        Logger log = LoggerFactory.getLogger(pjp.getTarget().getClass());
        log.trace("Method with signature: " + pjp.getTarget().getClass().getSimpleName() + "." + pjp.getSignature().getName() + "()");
        log.trace("With arguments: " + Arrays.toString(pjp.getArgs()));

        // You don't want the logger to consume the exception, so if we catch one here
        // we will log it and rethrow it:
        try {
            result = pjp.proceed();
        } catch (Throwable t) {
            logError(log,t);
            throw t;
        }
        log.trace("Method returned: " + result);
        return result;
    }

    private void logError(Logger log, Throwable t) {
        log.error("Exception thrown: " + t);
        for(StackTraceElement element : t.getStackTrace()) {
            log.warn(element.toString());
        }

        // If the exception has a cause, log the cause using recursion:
        if(t.getCause() != null) {
            logError(log, t.getCause());
        }
    }
}