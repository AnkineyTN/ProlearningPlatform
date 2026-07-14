package com.cabybara.prolearningplatform.dto.internal;

public record CardContent(String front, String back) {
    public interface Projection {
        String getFront();
        String getBack();
    }
}
