package com.bugtracker.server.dto;

public record FileDownload(String fileName, String contentType, byte[] content) {
}
