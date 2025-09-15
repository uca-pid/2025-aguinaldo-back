package com.medibook.api.mapper;

import com.medibook.api.dto.Turn.TurnCreateRequestDTO;
import com.medibook.api.dto.Turn.TurnResponseDTO;
import com.medibook.api.entity.TurnAssigned;
import com.medibook.api.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TurnAssignedMapper {

    public TurnAssigned toEntity(TurnCreateRequestDTO dto, User doctor) {
        return TurnAssigned.builder()
                .doctor(doctor)
                .scheduledAt(dto.getScheduledAt())
                .status("AVAILABLE")
                .build();
    }

    public TurnResponseDTO toDTO(TurnAssigned turn) {
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
                .build();
    }
}
