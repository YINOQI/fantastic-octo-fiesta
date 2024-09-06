package com.lwl.social_media_platform.common.exception;

public class ServiceException extends AbstractException {
    public ServiceException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ServiceException(String message) {
        this(message, null);

    }
}
