package com.bugtracker.server.storage;

import com.bugtracker.server.dto.FileUpload;

public interface FileStorage {
    String save(FileUpload file);

    byte[] open(String storageKey);

    void delete(String storageKey);
}
