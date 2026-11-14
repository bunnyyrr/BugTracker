package com.bugtracker.server.exception;

public class UnauthorizedException extends AppException {

    public UnauthorizedException(String message) {
        super(message);
    }
}