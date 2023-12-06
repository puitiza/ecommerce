package com.ecommerce.authservice.component.exception.errors;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GlobalExceptionErrors {

    NO_ITEM_FOUND("item.absent.msg", "item.absent.code"),
    VALIDATION_FIELD("user.validation.field.msg", "user.validation.field.code"),
    GLOBAL_ERROR("user.global.error.msg", "user.global.error.code"),
    AUTHORIZATION_ERROR("user.authorization.error.msg", "user.authorization.error.code"),
    DENIED_ACCESS_ERROR("user.denied.access.error.msg", Constants.DENIED_CODE),
    REFRESH_TOKEN_ERROR("user.refresh.token.error.msg", Constants.DENIED_CODE),
    REFRESH_TOKEN_EXPIRED_ERROR("user.refresh.token.expired.error.msg", Constants.DENIED_CODE);
    //STORE_FILE_ERROR("user.store.file.error.msg", Constants.FILE_CODE),
    //UPLOAD_FILE_ERROR("user.store.upload.file.error.msg", Constants.FILE_CODE);

    public final String key;
    public final String code;

    private static class Constants {
        public static final String DENIED_CODE = "user.denied.access.error.code";
    }

}
