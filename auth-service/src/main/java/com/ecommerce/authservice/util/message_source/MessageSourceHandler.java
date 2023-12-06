package com.ecommerce.authservice.util.message_source;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public record MessageSourceHandler(MessageSource messageSource) {

    public String getLocalMessage(String key, String... params) {
        return messageSource.getMessage(key,
                params,
                Locale.US);
    }

    public String getLocalMessage(String code) {
        return messageSource.getMessage(code, null, Locale.US);

    }
}
