package com.bugtracker.server.exception;

public class AppException extends RuntimeException{
    private final String field;

    public AppException(String message){
        this(message, null);
    }
    public AppException(String message, String field) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
