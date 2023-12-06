package com.ecommerce.authservice.component.exception.handler;

import java.time.ZonedDateTime;

public abstract class HandledException extends RuntimeException {

    protected HandledException() {
        super();
    }

    protected HandledException(Throwable e) {
        super(e);
    }

    protected HandledException(String e) {
        super(e);
    }

    public abstract String getErrorCode();

    public ZonedDateTime getZoneDateTime() {
        return ZonedDateTime.now();
    }

}
