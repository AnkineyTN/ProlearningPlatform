package com.cabybara.prolearningplatform.model.fcm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FCMMessage {
    private String subject;

    private String content;

    // private String image;

    private Map<String, String> data;

    private List<String> registrationTokens;
}
