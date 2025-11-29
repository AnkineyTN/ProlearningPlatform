package com.cabybara.prolearningplatform.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

@Aspect
@Component
public class NormalizeDtoAspect {

    @Pointcut("execution(* com.cabybara.prolearningplatform.controller.*.*(..))")
    public void executionPointcut() {}

    @Around("executionPointcut()")
    public Object trimDtoParameters(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();

        for (Object arg : args) {
            if (arg != null && !arg.getClass().isPrimitive() && !(arg instanceof String)) {
                normalizeObject(arg);
            }
        }

        return joinPoint.proceed();
    }

    private void normalizeObject(Object target) throws IllegalAccessException, InvocationTargetException {
        for (Field field : target.getClass().getDeclaredFields()) {

            if (field.getType().equals(String.class) && !field.getName().equals("password")) {

                field.setAccessible(true);

                String value = (String) field.get(target);

                if (value != null) {
                    field.set(target, value.trim());
                }
            }
        }
    }
}