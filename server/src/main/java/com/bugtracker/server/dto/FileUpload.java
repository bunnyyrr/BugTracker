package com.bugtracker.server.dto;

public record FileUpload(String fileName, String contentType, byte[] content) {

    public long getSize() {
        return content.length;
    }
}
