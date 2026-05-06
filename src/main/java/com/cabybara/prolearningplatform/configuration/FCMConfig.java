package com.cabybara.prolearningplatform.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.*;

@Configuration
@RequiredArgsConstructor
public class FCMConfig {
    @Value("${firebase.googleCredentials}")
    private String firebaseConfigPath;



    @Bean
    FirebaseMessaging firebaseMessaging() throws IOException {
        InputStream serviceAccount = null;

        try {
            File file = new File(firebaseConfigPath);
            if (file.exists() && file.isFile()) {
                serviceAccount = new FileInputStream(file);
            }
            else {
                ClassPathResource resource = new ClassPathResource(firebaseConfigPath);
                if (resource.exists()) {
                    serviceAccount = resource.getInputStream();
                }
            }

            if (serviceAccount == null) {
                throw new FileNotFoundException("Cannot find Firebase credentials at: " + firebaseConfigPath);
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            return FirebaseMessaging.getInstance();

        } finally {
            if (serviceAccount != null) {
                serviceAccount.close();
            }
        }
    }
}

