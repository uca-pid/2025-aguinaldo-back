package com.medibook.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupabaseStorageServiceImpl implements SupabaseStorageService {

    private final S3Client s3Client;

    @Value("${supabase.s3.endpoint}")
    private String supabaseS3Endpoint;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB in bytes

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png"
    );

    @Override
    public Mono<String> uploadFile(String bucketName, String fileName, MultipartFile file) {
        return Mono.fromCallable(() -> {
            validateFile(file);

            try {
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(fileName)
                        .contentType(file.getContentType())
                        .contentLength(file.getSize())
                        .build();

                s3Client.putObject(putObjectRequest,
                        RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

                log.info("File uploaded successfully: {}", fileName);
                return getPublicUrl(bucketName, fileName);

            } catch (IOException e) {
                log.error("Error uploading file {}: {}", fileName, e.getMessage());
                throw new RuntimeException("Error uploading file: " + e.getMessage());
            }
        });
    }

    @Override
    public Mono<Void> deleteFile(String bucketName, String fileName) {
        return Mono.fromRunnable(() -> {
            try {
                DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(fileName)
                        .build();

                s3Client.deleteObject(deleteObjectRequest);
                log.info("File deleted successfully: {}", fileName);

            } catch (Exception e) {
                log.error("Error deleting file {}: {}", fileName, e.getMessage());
                throw new RuntimeException("Error deleting file: " + e.getMessage());
            }
        });
    }

    @Override
    public String getPublicUrl(String bucketName, String fileName) {
        String projectUrl = supabaseS3Endpoint.replace("/storage/v1/s3", "");
        return String.format("%s/storage/v1/object/public/%s/%s", projectUrl, bucketName, fileName);
    }

    @Override
    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("File type not allowed. Only PDF, JPG, and PNG files are accepted");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = getFileExtension(originalFilename).toLowerCase();
            if (!Arrays.asList("pdf", "jpg", "jpeg", "png").contains(extension)) {
                throw new IllegalArgumentException("File extension not allowed. Only .pdf, .jpg, .jpeg, and .png files are accepted");
            }
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex == -1 ? "" : filename.substring(lastDotIndex + 1);
    }
}