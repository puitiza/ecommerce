package com.ecommerce.userservice.configuration.exception.handler;

public abstract class HandledException extends RuntimeException {

   /* protected HandledException() {
        super();
    }

    protected HandledException(Throwable e) {
        super(e);
    }

    protected HandledException(String e) {
        super(e);
    }

     public ZonedDateTime getZoneDateTime() {
        return ZonedDateTime.now();
    }

    */

    public abstract String getErrorCode();

}
