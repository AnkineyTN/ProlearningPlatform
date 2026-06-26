package com.cabybara.prolearningplatform.service.permission.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark Controller endpoints that consume AI resources.
 * Intercepted by {@code AiRateLimitAspect} to perform rate-limiting checks via Redis.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AiRateLimit {
    /**
     * The type of AI API. E.g. "AI_GENERATION" or "AI_INTERACTIVE".
     * Used as a key in the application configuration to lookup free/pro limits.
     */
    String type() default "AI_GENERATION";
}
