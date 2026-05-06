package com.cabybara.prolearningplatform.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Aspect
@Component
public class NormalizeDtoAspect {

    @Pointcut("execution(* com.cabybara.prolearningplatform.controller.*.*(..))")
    public void executionPointcut() {}

    @Around("executionPointcut()")
    public Object trimDtoParameters(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < args.length; i++) {
            if (args[i] != null && !isBuiltInType(args[i])) {
                args[i] = normalizeObject(args[i]);
            }
        }
        return joinPoint.proceed(args);
    }

    private boolean isBuiltInType(Object obj) {
        Class<?> clazz = obj.getClass();
        if (clazz.isPrimitive() || obj instanceof String || obj instanceof Number || obj instanceof Boolean) {
            return true;
        }
        String name = clazz.getName();
        return name.startsWith("org.springframework") || name.startsWith("org.apache") || name.startsWith("java.");
    }

    private Object normalizeObject(Object target) throws Exception {
        if (target == null) return null;
        Class<?> clazz = target.getClass();
        if (!isDtoClass(clazz)) return target;
        if (clazz.isRecord()) return normalizeRecord(target, clazz);
        normalizeBean(target, clazz);
        return target;
    }

    // Records are immutable — rebuild a new instance with normalized component values.
    private Object normalizeRecord(Object target, Class<?> clazz) throws Exception {
        RecordComponent[] components = clazz.getRecordComponents();
        Object[] args = new Object[components.length];
        for (int i = 0; i < components.length; i++) {
            Field field = clazz.getDeclaredField(components[i].getName());
            field.setAccessible(true);
            args[i] = normalizeValue(field.get(target), field.getName());
        }
        Constructor<?> ctor = clazz.getDeclaredConstructor(
                Arrays.stream(components).map(RecordComponent::getType).toArray(Class[]::new));
        return ctor.newInstance(args);
    }

    // Mutable beans — set normalized values back in place.
    private void normalizeBean(Object target, Class<?> clazz) throws Exception {
        for (Field field : clazz.getDeclaredFields()) {
            if (isSkippableField(field)) continue;
            field.setAccessible(true);
            Object normalized = normalizeValue(field.get(target), field.getName());
            field.set(target, normalized);
        }
    }

    // Central dispatch: handles String trimming, List<DTO>, and nested DTO objects.
    private Object normalizeValue(Object value, String fieldName) throws Exception {
        if (value == null) return null;
        if (value instanceof String str) {
            return "password".equals(fieldName) ? str : str.trim();
        }
        if (value instanceof List<?> list) {
            return normalizeList(list);
        }
        if (isDtoClass(value.getClass())) {
            return normalizeObject(value);
        }
        return value;
    }

    // Normalizes a List only when its elements are DTO objects.
    // Non-DTO lists (List<Long>, List<String>, etc.) are returned as-is.
    private List<?> normalizeList(List<?> list) throws Exception {
        if (list.isEmpty()) return list;
        Object sample = null;
        for (Object item : list) {
            if (item != null) { sample = item; break; }
        }
        if (sample == null || !isDtoClass(sample.getClass())) return list;

        List<Object> result = new ArrayList<>(list.size());
        for (Object item : list) {
            result.add(item != null ? normalizeObject(item) : null);
        }
        return result;
    }

    private boolean isDtoClass(Class<?> clazz) {
        String name = clazz.getSimpleName();
        return name.contains("Dto") || name.contains("DTO");
    }

    private boolean isSkippableField(Field field) {
        int mod = field.getModifiers();
        return field.isEnumConstant() || Modifier.isStatic(mod) || Modifier.isFinal(mod);
    }
}
