package com.cabybara.prolearningplatform.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorMetadata {
    private final String code;
    private final String path;
}