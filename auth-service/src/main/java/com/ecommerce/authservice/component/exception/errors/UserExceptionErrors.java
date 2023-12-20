package com.ecommerce.authservice.component.exception.errors;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserExceptionErrors {

    USERNAME_FOUND("user.exist.by.username.msg", Constants.EXIST_USERNAME_CODE),
    EMAIL_FOUND("user.exist.by.email.error.msg", Constants.EXIST_USERNAME_CODE),
    ROLE_NOT_FOUND("user.role.error.msg", Constants.EXIST_USERNAME_CODE);

    public final String key;
    public final String code;

    private static class Constants {
        public static final String EXIST_USERNAME_CODE = "user.exist.by.username.code";
    }
}
