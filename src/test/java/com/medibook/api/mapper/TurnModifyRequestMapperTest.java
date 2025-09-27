package com.medibook.api.mapper;

import com.medibook.api.dto.Turn.TurnModifyRequestResponseDTO;
import com.medibook.api.entity.TurnAssigned;
import com.medibook.api.entity.TurnModifyRequest;
import com.medibook.api.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TurnModifyRequestMapperTest {

    private TurnModifyRequestMapper mapper;
    private TurnModifyRequest entity;
    private TurnAssigned turnAssigned;
    private User patient;
    private User doctor;

    @BeforeEach
    void setUp() {
        mapper = new TurnModifyRequestMapper();
        
        patient = new User();
        patient.setId(UUID.randomUUID());
        
        doctor = new User();
        doctor.setId(UUID.randomUUID());
        
        turnAssigned = new TurnAssigned();
        turnAssigned.setId(UUID.randomUUID());
        
        entity = TurnModifyRequest.builder()
                .id(UUID.randomUUID())
                .turnAssigned(turnAssigned)
                .patient(patient)
                .doctor(doctor)
                .currentScheduledAt(OffsetDateTime.now())
                .requestedScheduledAt(OffsetDateTime.now().plusDays(1))
                .status("PENDING")
                .build();
    }

    @Test
    void toResponseDTO_WithValidEntity_ShouldReturnCorrectDTO() {
        TurnModifyRequestResponseDTO result = mapper.toResponseDTO(entity);
        
        assertNotNull(result);
        assertEquals(entity.getId(), result.getId());
        assertEquals(entity.getTurnAssigned().getId(), result.getTurnId());
        assertEquals(entity.getPatient().getId(), result.getPatientId());
        assertEquals(entity.getDoctor().getId(), result.getDoctorId());
        assertEquals(entity.getCurrentScheduledAt(), result.getCurrentScheduledAt());
        assertEquals(entity.getRequestedScheduledAt(), result.getRequestedScheduledAt());
        assertEquals(entity.getStatus(), result.getStatus());
    }

    @Test
    void toResponseDTO_WithNullEntity_ShouldReturnNull() {
        TurnModifyRequestResponseDTO result = mapper.toResponseDTO(null);
        
        assertNull(result);
    }
}