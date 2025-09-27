package com.medibook.api.mapper;

import com.medibook.api.dto.Turn.TurnModifyRequestResponseDTO;
import com.medibook.api.entity.TurnModifyRequest;
import org.springframework.stereotype.Component;

@Component
public class TurnModifyRequestMapper {
    
    public TurnModifyRequestResponseDTO toResponseDTO(TurnModifyRequest entity) {
        if (entity == null) {
            return null;
        }
        
        return new TurnModifyRequestResponseDTO(
                entity.getId(),
                entity.getTurnAssigned().getId(),
                entity.getPatient().getId(),
                entity.getDoctor().getId(),
                entity.getCurrentScheduledAt(),
                entity.getRequestedScheduledAt(),
                entity.getStatus()
        );
    }
}