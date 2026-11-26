package com.bugtracker.server.dto;

public record BugQuery(BugListFilter filter, String text, int page, int pageSize) {
}