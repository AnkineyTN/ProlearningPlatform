package com.cabybara.prolearningplatform.aspect;

import com.cabybara.prolearningplatform.exception.InvalidSortFieldException;
import com.cabybara.prolearningplatform.utils.ValidateSort;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Aspect
@Component
public class SortValidationAspect {

    @Before("@annotation(validateSort)")
    public void validateSort(JoinPoint joinPoint, ValidateSort validateSort) {

        Object[] args = joinPoint.getArgs();
        Pageable pageable = null;

        for (Object arg : args) {
            if (arg instanceof Pageable) {
                pageable = (Pageable) arg;
                break;
            }
        }

        if (pageable == null || pageable.getSort().isUnsorted()) {
            return;
        }

        List<String> allowedFields = Arrays.asList(validateSort.allowedFields());

        for (Sort.Order order : pageable.getSort()) {
            if (!allowedFields.contains(order.getProperty())) {
                throw new InvalidSortFieldException(
                        "Invalid sort field: " + order.getProperty()
                );
            }
        }
    }
}