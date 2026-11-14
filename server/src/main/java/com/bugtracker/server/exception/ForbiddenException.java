package com.bugtracker.server.exception;

public class ForbiddenException extends AppException {

    public ForbiddenException(String message) {
        super(message);
    }
}