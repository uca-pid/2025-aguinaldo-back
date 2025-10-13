package com.medibook.api.service;

import com.medibook.api.entity.TurnAssigned;
import com.medibook.api.entity.TurnFile;
import com.medibook.api.repository.TurnAssignedRepository;
import com.medibook.api.repository.TurnFileRepository;
import com.medibook.api.util.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TurnFileServiceImpl implements TurnFileService {

    private final TurnFileRepository turnFileRepository;
    private final SupabaseStorageService supabaseStorageService;
    private final TurnAssignedRepository turnAssignedRepository;
    private final NotificationService notificationService;
    
    private static final String BUCKET_NAME = "archivosTurnos";

    @Override
    public Mono<String> uploadTurnFile(UUID turnId, MultipartFile file) {
        log.info("Starting upload process for turnId: {}", turnId);
        
        if (turnFileRepository.existsByTurnId(turnId)) {
            return Mono.error(new IllegalStateException("Ya existe un archivo para este turno. Elimínalo antes de subir uno nuevo."));
        }

        String customFileName = turnId + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        log.info("Generated filename: {} for turnId: {}", customFileName, turnId);

        return supabaseStorageService.uploadFile(BUCKET_NAME, customFileName, file)
                .map(publicUrl -> {
                    TurnFile turnFile = TurnFile.builder()
                            .turnId(turnId)
                            .fileUrl(publicUrl)
                            .fileName(customFileName)
                            .build();
                    
                    turnFileRepository.save(turnFile);
                    log.info("File upload completed successfully for turnId: {}", turnId);
                    
                    // Create notification for the doctor
                    try {
                        Optional<TurnAssigned> turnOpt = turnAssignedRepository.findById(turnId);
                        if (turnOpt.isPresent()) {
                            TurnAssigned turn = turnOpt.get();
                            if (turn.getDoctor() != null && turn.getPatient() != null) {
                                String appointmentDate = DateTimeUtils.formatDate(turn.getScheduledAt());
                                String appointmentTime = DateTimeUtils.formatTime(turn.getScheduledAt());
                                String patientName = turn.getPatient().getName() + " " + turn.getPatient().getSurname();
                                
                                notificationService.createPatientFileUploadedNotification(
                                    turn.getDoctor().getId(),
                                    turnId,
                                    patientName,
                                    appointmentDate,
                                    appointmentTime,
                                    file.getOriginalFilename()
                                );
                                
                                log.info("Notification created for doctor {} about file upload by patient {}", 
                                        turn.getDoctor().getId(), turn.getPatient().getId());
                            }
                        }
                    } catch (Exception e) {
                        log.error("Error creating notification for file upload: {}", e.getMessage());
                        // Don't fail the upload if notification fails
                    }
                    
                    return "{\"url\":\"" + publicUrl + "\", \"fileName\":\"" + customFileName + "\"}";
                })
                .doOnError(error -> log.error("Error uploading turn file for turnId {}: {}", turnId, error.getMessage()));
    }

    @Override
    public Mono<Void> deleteTurnFile(UUID turnId) {
        log.info("Starting delete process for turnId: {}", turnId);
        
        return Mono.fromCallable(() -> turnFileRepository.findByTurnId(turnId))
                .flatMap(optionalTurnFile -> {
                    if (optionalTurnFile.isEmpty()) {
                        log.warn("No file found in database for turnId: {}", turnId);
                        return Mono.error(new IllegalArgumentException("Archivo no encontrado"));
                    }
                    
                    TurnFile turnFile = optionalTurnFile.get();
                    String fileName = turnFile.getFileName();
                    log.info("Found file in database: {} for turnId: {}", fileName, turnId);
                    
                    return supabaseStorageService.deleteFile(BUCKET_NAME, fileName)
                            .then(Mono.fromRunnable(() -> {
                                log.info("File {} deleted successfully from Supabase, now deleting from database", fileName);
                                turnFileRepository.deleteByTurnId(turnId);
                                log.info("Database record deleted successfully for turnId: {}", turnId);
                            }));
                })
                .then() // Convert to Mono<Void>
                .doOnError(error -> log.error("Error deleting turn file for turnId {}: {}", turnId, error.getMessage()));
    }

    @Override
    public Optional<TurnFile> getTurnFileInfo(UUID turnId) {
        log.debug("Getting file info for turnId: {}", turnId);
        return turnFileRepository.findByTurnId(turnId);
    }

    @Override
    public boolean fileExistsForTurn(UUID turnId) {
        log.debug("Checking if file exists for turnId: {}", turnId);
        return turnFileRepository.existsByTurnId(turnId);
    }
}