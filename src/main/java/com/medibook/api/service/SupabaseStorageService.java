package com.medibook.api.service;

import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

public interface SupabaseStorageService {

    Mono<String> uploadFile(String bucketName, String fileName, MultipartFile file);

    Mono<Void> deleteFile(String bucketName, String fileName);

    String getPublicUrl(String bucketName, String fileName);

    void validateFile(MultipartFile file);
}