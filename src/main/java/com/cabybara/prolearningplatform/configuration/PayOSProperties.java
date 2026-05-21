package com.cabybara.prolearningplatform.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "payos")
@Getter
@Setter
public class PayOSProperties {
    private String clientId;
    private String apiKey;
    private String checksumKey;
    private String baseUrl;
    private String webhookUrl;
    private String returnUrlWeb;
    private String cancelUrlWeb;
    private String returnUrlMobile;
    private String cancelUrlMobile;
}
