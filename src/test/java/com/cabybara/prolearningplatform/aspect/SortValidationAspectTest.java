package com.cabybara.prolearningplatform.aspect;

import com.cabybara.prolearningplatform.exception.InvalidSortFieldException;
import com.cabybara.prolearningplatform.utils.ValidateSort;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SortValidationAspectTest {

    private final SortValidationAspect aspect = new SortValidationAspect();

    @Test
    void validateSortAllowsWhitelistedFields() throws Exception {
        JoinPoint joinPoint = mock(JoinPoint.class);
        when(joinPoint.getArgs()).thenReturn(new Object[]{
                PageRequest.of(0, 20, Sort.by("title").ascending().and(Sort.by("createdAt").descending()))
        });

        assertDoesNotThrow(() -> aspect.validateSort(joinPoint, validateSortAnnotation()));
    }

    @Test
    void validateSortRejectsUnknownField() throws Exception {
        JoinPoint joinPoint = mock(JoinPoint.class);
        when(joinPoint.getArgs()).thenReturn(new Object[]{
                PageRequest.of(0, 20, Sort.by("unknown"))
        });

        InvalidSortFieldException exception = assertThrows(
                InvalidSortFieldException.class,
                () -> aspect.validateSort(joinPoint, validateSortAnnotation())
        );

        assertEquals("Invalid sort field: unknown", exception.getMessage());
    }

    @Test
    void validateSortSkipsWhenPageableMissingOrUnsorted() throws Exception {
        JoinPoint noPageableJoinPoint = mock(JoinPoint.class);
        when(noPageableJoinPoint.getArgs()).thenReturn(new Object[]{"not-pageable"});

        JoinPoint unsortedJoinPoint = mock(JoinPoint.class);
        when(unsortedJoinPoint.getArgs()).thenReturn(new Object[]{PageRequest.of(0, 20)});

        assertDoesNotThrow(() -> aspect.validateSort(noPageableJoinPoint, validateSortAnnotation()));
        assertDoesNotThrow(() -> aspect.validateSort(unsortedJoinPoint, validateSortAnnotation()));
    }

    private ValidateSort validateSortAnnotation() throws NoSuchMethodException {
        Method method = TestHandler.class.getDeclaredMethod("handle");
        return method.getAnnotation(ValidateSort.class);
    }

    private static class TestHandler {
        @ValidateSort(allowedFields = {"title", "createdAt"})
        void handle() {
        }
    }
}
