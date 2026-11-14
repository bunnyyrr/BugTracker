package com.bugtracker.server.api.error;

public record ApiError(String code, String message, String field) {
}