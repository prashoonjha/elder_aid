package com.elderaid.platform.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Deliberately just two methods. Whatever backs this - local disk now,
 * S3 later - the rest of the app only ever needs to store a file and get a
 * key back, or read a file back out by that key.
 */
public interface FileStorageService {

    String store(MultipartFile file, String subDirectory);

    byte[] read(String storageKey);
}
