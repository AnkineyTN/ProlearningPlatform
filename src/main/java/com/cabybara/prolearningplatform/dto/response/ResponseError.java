package com.cabybara.prolearningplatform.dto.response;

import org.springframework.http.HttpStatus;

public class ResponseError<T> extends ResponseData<T> {
    public ResponseError(HttpStatus status, String message) {
        super(status.value(), message);
    }

    public ResponseError(int status, String message) {
        super(status, message);
    }
}
