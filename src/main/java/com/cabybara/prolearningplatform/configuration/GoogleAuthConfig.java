package com.cabybara.prolearningplatform.configuration;

import com.cabybara.prolearningplatform.repository.TokenDatabaseRepository;
import com.cabybara.prolearningplatform.utils.DatabaseDataStoreFactory;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.store.DataStoreFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class GoogleAuthConfig {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String CLIENT_ID;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String CLIENT_SECRET;

    @Value("${spring.security.oauth2.client.registration.google.scope}")
    private List<String> SCOPES;

    private final TokenDatabaseRepository<StoredCredential> googleCredentialTokenDatabaseRepository;

    @Bean
    public GoogleAuthorizationCodeFlow googleFlow() throws IOException {
        DataStoreFactory dataStoreFactory = new DatabaseDataStoreFactory(googleCredentialTokenDatabaseRepository);

        final Set<String> CORE_SCOPES = Set.of("openid", "email", "profile");

        Collection<String> scopes = SCOPES.stream()
                .map(String::trim)
                .map(scope -> {
                    if (CORE_SCOPES.contains(scope)) {
                        return scope;
                    }
                    if (!scope.startsWith("https://")) {
                        return "https://www.googleapis.com/auth/" + scope;
                    }
                    return scope;
                })
                .collect(Collectors.toSet());

        return new GoogleAuthorizationCodeFlow.Builder(
                new NetHttpTransport(),
                new JacksonFactory(),
                CLIENT_ID,
                CLIENT_SECRET,
                scopes)
                .setDataStoreFactory(dataStoreFactory)
                .setAccessType("offline")
                .setApprovalPrompt("force")
                .build();
    }
}
