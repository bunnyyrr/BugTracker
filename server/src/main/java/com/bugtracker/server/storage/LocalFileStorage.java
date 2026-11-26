package com.bugtracker.server.storage;

import com.bugtracker.server.dto.FileUpload;
import com.bugtracker.server.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class LocalFileStorage implements FileStorage {

    private final Path root;

    public LocalFileStorage(@Value("${app.storage.path:./uploads}") String path) {
        this.root = Paths.get(path).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String save(FileUpload file) {
        String storageKey = UUID.randomUUID().toString();
        try {
            Files.write(root.resolve(storageKey), file.content());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return storageKey;
    }

    @Override
    public byte[] open(String storageKey) {
        try {
            return Files.readAllBytes(resolve(storageKey));
        } catch (NoSuchFileException e) {
            throw new NotFoundException("Файл не найден");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            Files.deleteIfExists(resolve(storageKey));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path resolve(String storageKey) {
        Path path = root.resolve(storageKey).normalize();
        if (!path.startsWith(root)) {
            throw new NotFoundException("Файл не найден");
        }
        return path;
    }
}
