package com.cabybara.prolearningplatform.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FCMMessage {
    private String subject;

    private String content;

    private String imageUrl;

    @Builder.Default
    private Map<String, String> data = new HashMap<>();

    private List<String> registrationTokens;

    public FCMMessage addData(String key, String value) {
        if (this.data == null) {
            this.data = new HashMap<>();
        }
        this.data.put(key, value);
        return this;
    }

    public boolean hasValidTokens() {
        return registrationTokens != null && !registrationTokens.isEmpty();
    }
}

