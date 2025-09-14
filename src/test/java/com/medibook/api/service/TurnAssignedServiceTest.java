package com.medibook.api.service;

import com.medibook.api.dto.TurnCreateRequestDTO;
import com.medibook.api.dto.TurnResponseDTO;
import com.medibook.api.entity.TurnAssigned;
import com.medibook.api.entity.User;
import com.medibook.api.mapper.TurnAssignedMapper;
import com.medibook.api.repository.TurnAssignedRepository;
import com.medibook.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TurnAssignedServiceTest {

    @Mock
    private TurnAssignedRepository turnRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private TurnAssignedMapper mapper;

    @InjectMocks
    private TurnAssignedService turnAssignedService;

    private UUID doctorId;
    private UUID patientId;
    private UUID turnId;
    private User doctor;
    private User patient;
    private TurnCreateRequestDTO createRequest;
    private TurnAssigned turnEntity;
    private TurnResponseDTO turnResponse;
    private OffsetDateTime scheduledAt;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        turnId = UUID.randomUUID();
        scheduledAt = OffsetDateTime.now().plusDays(1);

        doctor = new User();
        doctor.setId(doctorId);
        doctor.setName("Dr. Hugo");
        doctor.setSurname("Martinez");
        doctor.setRole("DOCTOR");

        patient = new User();
        patient.setId(patientId);
        patient.setName("Juan");
        patient.setSurname("Perez");
        patient.setRole("PATIENT");

        createRequest = new TurnCreateRequestDTO();
        createRequest.setDoctorId(doctorId);
        createRequest.setPatientId(patientId);
        createRequest.setScheduledAt(scheduledAt);

        turnEntity = TurnAssigned.builder()
                .id(turnId)
                .doctor(doctor)
                .patient(patient)
                .scheduledAt(scheduledAt)
                .status("SCHEDULED")
                .build();

        turnResponse = TurnResponseDTO.builder()
                .id(turnId)
                .doctorId(doctorId)
                .doctorName("Dr. Hugo Martinez")
                .patientId(patientId)
                .patientName("Juan Perez")
                .scheduledAt(scheduledAt)
                .status("SCHEDULED")
                .build();
    }

    @Test
    void createTurn_Success() {
        // Given
        when(userRepo.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(userRepo.findById(patientId)).thenReturn(Optional.of(patient));
        when(turnRepo.existsByDoctor_IdAndScheduledAt(doctorId, scheduledAt)).thenReturn(false);
        when(turnRepo.save(any(TurnAssigned.class))).thenReturn(turnEntity);
        when(mapper.toDTO(turnEntity)).thenReturn(turnResponse);

        // When
        TurnResponseDTO result = turnAssignedService.createTurn(createRequest);

        // Then
        assertNotNull(result);
        assertEquals(turnId, result.getId());
        assertEquals(doctorId, result.getDoctorId());
        assertEquals(patientId, result.getPatientId());
        assertEquals("SCHEDULED", result.getStatus());
        assertEquals(scheduledAt, result.getScheduledAt());

        verify(userRepo).findById(doctorId);
        verify(userRepo).findById(patientId);
        verify(turnRepo).existsByDoctor_IdAndScheduledAt(doctorId, scheduledAt);
        verify(turnRepo).save(any(TurnAssigned.class));
        verify(mapper).toDTO(turnEntity);
    }

    @Test
    void createTurn_DoctorNotFound_ThrowsException() {
        // Given
        when(userRepo.findById(doctorId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            turnAssignedService.createTurn(createRequest);
        });

        assertEquals("Doctor not found", exception.getMessage());
        verify(userRepo).findById(doctorId);
        verify(userRepo, never()).findById(patientId);
        verify(turnRepo, never()).existsByDoctor_IdAndScheduledAt(any(), any());
        verify(turnRepo, never()).save(any());
    }

    @Test
    void createTurn_PatientNotFound_ThrowsException() {
        // Given
        when(userRepo.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(userRepo.findById(patientId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            turnAssignedService.createTurn(createRequest);
        });

        assertEquals("Patient not found", exception.getMessage());
        verify(userRepo).findById(doctorId);
        verify(userRepo).findById(patientId);
        verify(turnRepo, never()).existsByDoctor_IdAndScheduledAt(any(), any());
        verify(turnRepo, never()).save(any());
    }

    @Test
    void createTurn_SlotAlreadyTaken_ThrowsException() {
        // Given
        when(userRepo.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(userRepo.findById(patientId)).thenReturn(Optional.of(patient));
        when(turnRepo.existsByDoctor_IdAndScheduledAt(doctorId, scheduledAt)).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            turnAssignedService.createTurn(createRequest);
        });

        assertEquals("Time slot is already taken", exception.getMessage());
        verify(userRepo).findById(doctorId);
        verify(userRepo).findById(patientId);
        verify(turnRepo).existsByDoctor_IdAndScheduledAt(doctorId, scheduledAt);
        verify(turnRepo, never()).save(any());
    }

    @Test
    void reserveTurn_Success() {
        // Given
        TurnAssigned availableTurn = TurnAssigned.builder()
                .id(turnId)
                .doctor(doctor)
                .patient(null)
                .scheduledAt(scheduledAt)
                .status("AVAILABLE")
                .build();

        TurnAssigned reservedTurn = TurnAssigned.builder()
                .id(turnId)
                .doctor(doctor)
                .patient(patient)
                .scheduledAt(scheduledAt)
                .status("RESERVED")
                .build();

        when(turnRepo.findById(turnId)).thenReturn(Optional.of(availableTurn));
        when(userRepo.findById(patientId)).thenReturn(Optional.of(patient));
        when(turnRepo.save(any(TurnAssigned.class))).thenReturn(reservedTurn);

        // When
        TurnAssigned result = turnAssignedService.reserveTurn(turnId, patientId);

        // Then
        assertNotNull(result);
        assertEquals(turnId, result.getId());
        assertEquals(patient, result.getPatient());
        assertEquals("RESERVED", result.getStatus());

        verify(turnRepo).findById(turnId);
        verify(userRepo).findById(patientId);
        verify(turnRepo).save(any(TurnAssigned.class));
    }

    @Test
    void reserveTurn_TurnNotFound_ThrowsException() {
        // Given
        when(turnRepo.findById(turnId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            turnAssignedService.reserveTurn(turnId, patientId);
        });

        assertEquals("Turn not found", exception.getMessage());
        verify(turnRepo).findById(turnId);
        verify(userRepo, never()).findById(any());
        verify(turnRepo, never()).save(any());
    }

    @Test
    void reserveTurn_TurnNotAvailable_ThrowsException() {
        // Given
        TurnAssigned scheduledTurn = TurnAssigned.builder()
                .id(turnId)
                .doctor(doctor)
                .patient(patient)
                .scheduledAt(scheduledAt)
                .status("SCHEDULED")
                .build();

        when(turnRepo.findById(turnId)).thenReturn(Optional.of(scheduledTurn));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            turnAssignedService.reserveTurn(turnId, patientId);
        });

        assertEquals("Turn is not available", exception.getMessage());
        verify(turnRepo).findById(turnId);
        verify(userRepo, never()).findById(any());
        verify(turnRepo, never()).save(any());
    }

    @Test
    void reserveTurn_PatientNotFound_ThrowsException() {
        // Given
        TurnAssigned availableTurn = TurnAssigned.builder()
                .id(turnId)
                .doctor(doctor)
                .patient(null)
                .scheduledAt(scheduledAt)
                .status("AVAILABLE")
                .build();

        when(turnRepo.findById(turnId)).thenReturn(Optional.of(availableTurn));
        when(userRepo.findById(patientId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            turnAssignedService.reserveTurn(turnId, patientId);
        });

        assertEquals("Patient not found", exception.getMessage());
        verify(turnRepo).findById(turnId);
        verify(userRepo).findById(patientId);
        verify(turnRepo, never()).save(any());
    }
}