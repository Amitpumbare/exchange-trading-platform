package com.example.demo.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PerformanceAspect {

    private static final Logger log = LoggerFactory.getLogger(PerformanceAspect.class);

    @Around("execution(* com.example.demo.service..*(..))")
    public Object measureExecutionTime(ProceedingJoinPoint pjp) throws Throwable {

        String methodName = pjp.getSignature().toShortString();

        long start = System.currentTimeMillis();

        Object result = pjp.proceed();

        long end = System.currentTimeMillis();

        long timeTaken = end - start;

        log.info("⏱ {} executed in {} ms", methodName, timeTaken);

        return result;
    }
}
