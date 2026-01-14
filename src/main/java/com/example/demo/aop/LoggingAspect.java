package com.example.demo.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    // Log before controller + service methods
    @Before("execution(* com.example.demo.controller..*(..)) || execution(* com.example.demo.service..*(..))")
    public void logBefore(JoinPoint joinPoint) {

        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();

        log.info("➡️ Entering {} with arguments {}", methodName, args);
    }

    // Log after successful return
    @AfterReturning(
            pointcut = "execution(* com.example.demo.controller..*(..)) || execution(* com.example.demo.service..*(..))",
            returning = "result"
    )
    public void logAfter(JoinPoint joinPoint, Object result) {

        String methodName = joinPoint.getSignature().toShortString();

        log.info("✔️ Exiting {} with result {}", methodName, result);
    }
}
