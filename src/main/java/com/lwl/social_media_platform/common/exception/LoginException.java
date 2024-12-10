package com.lwl.social_media_platform.common.exception;

public class LoginException extends AbstractException{
    public LoginException(String message, Throwable throwable) {
        super(message,throwable);
    }

    public LoginException(String message) {
        this(message,null);
    }
}
