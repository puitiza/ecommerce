package com.ecommerce.userservice.configuration.exception.handler;

public abstract class HandledException extends RuntimeException {

   /* protected HandledException() {
        super();
    }

    protected HandledException(Throwable e) {
        super(e);
    }

    */

    protected HandledException(String message) {
        super(message);
    }

    public abstract String getErrorCode();

}
