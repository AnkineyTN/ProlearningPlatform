package com.cabybara.prolearningplatform.exception;

import lombok.Getter;

@Getter
public class EmailNotVerifiedException extends RuntimeException {

    public EmailNotVerifiedException() {
        super("Email has not been verified.");
    }
}