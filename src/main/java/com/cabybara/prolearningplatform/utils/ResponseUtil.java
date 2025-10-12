package com.cabybara.prolearningplatform.utils;

public class ResponseUtil {

    public static <T> ApiResponse<T> success(String message, T data, Object metadata) {
        return new ApiResponse<>("success", message, data, metadata);
    }

    public static <T> ApiResponse<T> error(String message, T data, Object metadata) {
        return new ApiResponse<>("error", message, data, metadata);
    }
}
