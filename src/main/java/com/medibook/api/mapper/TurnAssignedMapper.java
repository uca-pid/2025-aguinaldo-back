package com.medibook.api.mapper;

import com.medibook.api.dto.Turn.TurnCreateRequestDTO;
import com.medibook.api.dto.Turn.TurnResponseDTO;
import com.medibook.api.entity.TurnAssigned;
import com.medibook.api.entity.User;
import com.medibook.api.repository.RatingRepository;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TurnAssignedMapper {

    private final RatingRepository ratingRepository;

    public TurnAssigned toEntity(TurnCreateRequestDTO dto, User doctor) {
        return TurnAssigned.builder()
                .doctor(doctor)
                .scheduledAt(dto.getScheduledAt())
                .status("AVAILABLE")
                .build();
    }

    public TurnResponseDTO toDTO(TurnAssigned turn) {
        boolean isCompleted = "COMPLETED".equals(turn.getStatus());
        
        boolean needsPatientRating = false;
        boolean needsDoctorRating = false;
        
        if (turn.getPatient() != null && turn.getDoctor() != null) {
            // Patient can only rate completed turns
            if (isCompleted) {
                needsPatientRating = !ratingRepository.existsByTurnAssigned_IdAndRater_Id(
                    turn.getId(), turn.getPatient().getId());
            }
            
            // Doctor can only rate completed turns
            if (isCompleted) {
                needsDoctorRating = !ratingRepository.existsByTurnAssigned_IdAndRater_Id(
                    turn.getId(), turn.getDoctor().getId());
            }
        }
        
        return TurnResponseDTO.builder()
                .id(turn.getId())
                .doctorId(turn.getDoctor().getId())
                .doctorName(turn.getDoctor().getName() + " " + turn.getDoctor().getSurname())
                .doctorSpecialty(turn.getDoctor().getDoctorProfile() != null ? 
                    turn.getDoctor().getDoctorProfile().getSpecialty() : null)
                .patientId(turn.getPatient() != null ? turn.getPatient().getId() : null)
                .patientName(turn.getPatient() != null ? turn.getPatient().getName() + " " + turn.getPatient().getSurname() : null)
                .scheduledAt(turn.getScheduledAt())
                .status(turn.getStatus())
                .needsPatientRating(needsPatientRating)
                .needsDoctorRating(needsDoctorRating)
                .build();
    }
}
