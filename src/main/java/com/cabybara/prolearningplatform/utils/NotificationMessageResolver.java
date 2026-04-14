package com.cabybara.prolearningplatform.utils;

import com.cabybara.prolearningplatform.enums.UserLanguage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class NotificationMessageResolver {

    private final MessageSource messageSource;

    public String resolve(String key, UserLanguage language, Object... args) {
        return messageSource.getMessage(key, args, toLocale(language));
    }

    private Locale toLocale(UserLanguage language) {
        return language == UserLanguage.VI ? Locale.forLanguageTag("vi") : Locale.ENGLISH;
    }
}
