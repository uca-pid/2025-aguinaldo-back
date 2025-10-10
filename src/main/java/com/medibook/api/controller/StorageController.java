package com.medibook.api.controller;

import com.medibook.api.service.SupabaseStorageService;
import com.medibook.api.service.TurnFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
@Slf4j
public class StorageController {

    private final SupabaseStorageService supabaseStorageService;
    private final TurnFileService turnFileService;

    @PostMapping(value = "/upload-turn-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('PATIENT')")
    public Mono<ResponseEntity<String>> uploadTurnFile(
            @RequestParam("turnId") UUID turnId,
            @RequestParam("file") MultipartFile file) {

        return turnFileService.uploadTurnFile(turnId, file)
                .map(result -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(result))
                .onErrorResume(error -> {
                    log.error("Error uploading turn file: {}", error.getMessage());
                    return Mono.just(ResponseEntity.badRequest()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body("{\"error\":\"" + error.getMessage() + "\"}"));
                });
    }

    @DeleteMapping("/delete-turn-file/{turnId}")
    @PreAuthorize("hasRole('PATIENT')")
    public Mono<ResponseEntity<String>> deleteTurnFile(@PathVariable UUID turnId) {
        return turnFileService.deleteTurnFile(turnId)
                .then(Mono.just(ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Archivo eliminado exitosamente\"}")))
                .onErrorResume(error -> {
                    log.error("Error deleting turn file: {}", error.getMessage());
                    if (error.getMessage().contains("no encontrado")) {
                        return Mono.just(ResponseEntity.status(404)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body("{\"error\":\"" + error.getMessage() + "\"}"));
                    }
                    return Mono.just(ResponseEntity.status(500)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body("{\"error\":\"" + error.getMessage() + "\"}"));
                });
    }

    @GetMapping("/turn-file/{turnId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('PATIENT')")
    public ResponseEntity<String> getTurnFileInfo(@PathVariable UUID turnId) {
        return turnFileService.getTurnFileInfo(turnId)
                .map(turnFile -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"url\":\"" + turnFile.getFileUrl() + "\", \"fileName\":\"" + turnFile.getFileName() + "\", \"uploadedAt\":\"" + turnFile.getUploadedAt() + "\"}"))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('DOCTOR') or hasRole('PATIENT') or hasRole('ADMIN')")
    public Mono<ResponseEntity<String>> uploadFile(
            @RequestParam("bucket") String bucketName,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "fileName", required = false) String fileName) {

        String finalFileName = fileName != null ? fileName : file.getOriginalFilename();

        return supabaseStorageService.uploadFile(bucketName, finalFileName, file)
                .map(publicUrl -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"url\":\"" + publicUrl + "\"}"))
                .onErrorResume(error -> {
                    log.error("Error uploading file: {}", error.getMessage());
                    return Mono.just(ResponseEntity.badRequest()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body("{\"error\":\"" + error.getMessage() + "\"}"));
                });
    }

    @DeleteMapping("/delete/{bucketName}/{fileName}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public Mono<ResponseEntity<String>> deleteFile(
            @PathVariable String bucketName,
            @PathVariable String fileName) {

        return supabaseStorageService.deleteFile(bucketName, fileName)
                .then(Mono.just(ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"File deleted successfully\"}")))
                .onErrorResume(error -> {
                    log.error("Error deleting file: {}", error.getMessage());
                    return Mono.just(ResponseEntity.badRequest()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body("{\"error\":\"" + error.getMessage() + "\"}"));
                });
    }

    @GetMapping("/url/{bucketName}/{fileName}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<String> getPublicUrl(
            @PathVariable String bucketName,
            @PathVariable String fileName) {

        String publicUrl = supabaseStorageService.getPublicUrl(bucketName, fileName);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"url\":\"" + publicUrl + "\"}");
    }
}