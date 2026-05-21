package com.cabybara.prolearningplatform.configuration;

import com.cabybara.prolearningplatform.exception.JwtAuthEntryPoint;
import com.cabybara.prolearningplatform.service.auth.impl.JwtServiceImpl;
import com.cabybara.prolearningplatform.service.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${spring.security.jwt.secret}")
    private String JWT_SECRET;

    @Value("${spring.security.jwt.access-ttl-ms}")
    private Long JWT_EXPIRATION;

    private final JwtAuthEntryPoint unauthorizedHandler;
    private final RedisService redisService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // CORS is already handled by AppConfig.addCorsMappings()
        
        http.authorizeHttpRequests((authorize) -> authorize
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/swagger-resources/**").permitAll()
                .requestMatchers("/webjars/**").permitAll()
                .requestMatchers("/api-docs/**").permitAll()
                .requestMatchers("/auth/register/**").permitAll()
                .requestMatchers("/auth/login/**").permitAll()
                .requestMatchers("/auth/refresh/**").permitAll()
                .requestMatchers("/auth/google/**").permitAll()
                .requestMatchers("/auth/verify-email/**").permitAll()
                .requestMatchers("/auth/forgot-password/**").permitAll()
                .requestMatchers("/auth/verify-reset-otp/**").permitAll()
                .requestMatchers("/auth/reset-password/**").permitAll()
                .requestMatchers("/internal/**").permitAll()
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/social/**").permitAll()
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/webhook/payos").permitAll()
                .anyRequest().authenticated()
        );

        http.sessionManagement(
                session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS)
        );

        http.exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler));
        http.headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
        );

        http.csrf(AbstractHttpConfigurer::disable);

        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
                .decoder(jwtDecoder())
                .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                .authenticationEntryPoint(unauthorizedHandler)
        );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration builder) throws Exception {
        return builder.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(
            @Qualifier("userServiceImpl") UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(passwordEncoder);
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);

        return daoAuthenticationProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder delegate = NimbusJwtDecoder.withSecretKey(
                new SecretKeySpec(JWT_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256")
        ).build();
        return token -> {
            Jwt jwt = delegate.decode(token);
            String jti = jwt.getId();
            if (jti != null && redisService.hasKey(JwtServiceImpl.BLACKLIST_JTI_KEY_PREFIX + jti)) {
                throw new JwtException("Token has been revoked");
            }
            Object userIdClaim = jwt.getClaims().get("id");
            if (userIdClaim != null && redisService.hasKey(
                    com.cabybara.prolearningplatform.service.admin.impl.AdminUserManagementServiceImpl.BLOCKED_USER_KEY_PREFIX + userIdClaim)) {
                throw new JwtException("Account has been suspended");
            }
            return jwt;
        };
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter gac = new JwtGrantedAuthoritiesConverter();
        gac.setAuthoritiesClaimName("roles");
        gac.setAuthorityPrefix("");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(gac);
        return converter;
    }
}
