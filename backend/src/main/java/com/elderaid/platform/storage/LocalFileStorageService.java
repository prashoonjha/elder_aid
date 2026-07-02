package com.elderaid.platform.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {

    private final Path rootDirectory;

    public LocalFileStorageService(@Value("${app.storage.upload-dir}") String uploadDir) {
        this.rootDirectory = Paths.get(uploadDir).toAbsolutePath();
        try {
            Files.createDirectories(rootDirectory);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create upload directory: " + rootDirectory, e);
        }
    }

    @Override
    public String store(MultipartFile file, String subDirectory) {
        // Never trust the original filename for anything other than the
        // extension - a worker uploading "../../etc/passwd.png" should not
        // be able to write outside the upload root.
        String extension = extractExtension(file.getOriginalFilename());
        String generatedName = UUID.randomUUID() + extension;
        String key = subDirectory + "/" + generatedName;

        Path target = rootDirectory.resolve(key).normalize();
        if (!target.startsWith(rootDirectory)) {
            throw new IllegalArgumentException("Resolved file path escapes the upload directory");
        }

        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store uploaded file", e);
        }

        return key;
    }

    @Override
    public byte[] read(String storageKey) {
        Path target = rootDirectory.resolve(storageKey).normalize();
        if (!target.startsWith(rootDirectory)) {
            throw new IllegalArgumentException("Resolved file path escapes the upload directory");
        }
        try {
            return Files.readAllBytes(target);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read stored file: " + storageKey, e);
        }
    }

    @Override
    public void delete(String storageKey) {
        Path target = rootDirectory.resolve(storageKey).normalize();
        if (!target.startsWith(rootDirectory)) {
            throw new IllegalArgumentException("Resolved file path escapes the upload directory");
        }
        try {
            // deleteIfExists rather than delete - a missing file is not a
            // failure during account deletion, since the file may already
            // have been cleaned up or never successfully written.
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete stored file: " + storageKey, e);
        }
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.'));
    }
}
