package com.medibook.api.service;

import com.medibook.api.entity.TurnFile;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

public interface TurnFileService {
    
    Mono<String> uploadTurnFile(UUID turnId, MultipartFile file);
    
    Mono<Void> deleteTurnFile(UUID turnId);
    
    Optional<TurnFile> getTurnFileInfo(UUID turnId);
    
    boolean fileExistsForTurn(UUID turnId);
}
