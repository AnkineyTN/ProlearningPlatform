package com.cabybara.prolearningplatform.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;

@Aspect
@Component
public class NormalizeDtoAspect {

    @Pointcut("execution(* com.cabybara.prolearningplatform.controller.*.*(..))")
    public void executionPointcut() {}

    @Around("executionPointcut()")
    public Object trimDtoParameters(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg != null && !arg.getClass().isPrimitive() && !(arg instanceof String)) {
                Object normalized = normalizeObject(arg);
                args[i] = normalized;
            }
        }

        return joinPoint.proceed(args);
    }

    private Object normalizeObject(Object target) throws Exception {
        Class<?> clazz = target.getClass();

        // Nếu là record
        if (clazz.isRecord()) {
            RecordComponent[] components = clazz.getRecordComponents();
            Object[] args = new Object[components.length];

            for (int i = 0; i < components.length; i++) {
                Field field = clazz.getDeclaredField(components[i].getName());
                field.setAccessible(true);
                Object value = field.get(target);

                if (value instanceof String && !field.getName().equals("password")) {
                    value = ((String) value).trim();
                }

                args[i] = value;
            }

            // Tạo instance record mới với constructor
            Constructor<?> ctor = clazz.getDeclaredConstructor(Arrays.stream(components)
                    .map(RecordComponent::getType)
                    .toArray(Class[]::new));
            return ctor.newInstance(args);
        }

        // Nếu không phải record → vẫn dùng reflection set như cũ
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType().equals(String.class) && !field.getName().equals("password")) {
                field.setAccessible(true);
                String value = (String) field.get(target);
                if (value != null) {
                    field.set(target, value.trim());
                }
            }
        }

        return target;
    }
}