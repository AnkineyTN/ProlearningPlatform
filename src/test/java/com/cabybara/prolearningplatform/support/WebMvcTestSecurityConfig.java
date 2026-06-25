package com.cabybara.prolearningplatform.support;

import com.cabybara.prolearningplatform.exception.JwtAuthEntryPoint;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Lightweight security setup for {@code @WebMvcTest} slices.
 * <p>
 * The production {@link com.cabybara.prolearningplatform.configuration.SecurityConfig}
 * cannot be imported into a web slice because it wires beans that are absent there
 * (the {@code userServiceImpl} {@link org.springframework.security.core.userdetails.UserDetailsService},
 * a real {@code JwtDecoder}, {@code RedisService}, ...). This config replicates only what
 * controller slice tests need: the same public/authenticated URL boundary, method security
 * for {@code @PreAuthorize}, and the shared {@link JwtAuthEntryPoint} so unauthenticated
 * requests produce the same 401 envelope as production.
 * <p>
 * Authenticated requests are simulated with spring-security-test's {@code jwt()} request
 * post-processor, so no real {@code JwtDecoder} is required.
 */
@TestConfiguration
@EnableMethodSecurity
public class WebMvcTestSecurityConfig {

    @Bean
    public JwtAuthEntryPoint jwtAuthEntryPoint() {
        return new JwtAuthEntryPoint();
    }

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http, JwtAuthEntryPoint entryPoint) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/auth/register/**").permitAll()
                .requestMatchers("/auth/login/**").permitAll()
                .requestMatchers("/auth/refresh/**").permitAll()
                .requestMatchers("/auth/google/**").permitAll()
                .requestMatchers("/auth/verify-email/**").permitAll()
                .requestMatchers("/auth/forgot-password/**").permitAll()
                .requestMatchers("/auth/verify-reset-otp/**").permitAll()
                .requestMatchers("/auth/reset-password/**").permitAll()
                .anyRequest().authenticated()
        );
        http.exceptionHandling(exception -> exception.authenticationEntryPoint(entryPoint));
        return http.build();
    }
}
