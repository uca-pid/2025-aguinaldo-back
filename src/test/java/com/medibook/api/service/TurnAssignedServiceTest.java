
package com.medibook.api.service;

import com.medibook.api.dto.Turn.TurnCreateRequestDTO;
import com.medibook.api.dto.Turn.TurnResponseDTO;
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
import java.util.Arrays;
import java.util.List;
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

    @Mock
    private com.medibook.api.repository.TurnModifyRequestRepository turnModifyRequestRepository;

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
        doctor.setStatus("ACTIVE");

        patient = new User();
        patient.setId(patientId);
        patient.setName("Juan");
        patient.setSurname("Perez");
        patient.setRole("PATIENT");
        patient.setStatus("ACTIVE");

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
        when(userRepo.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(userRepo.findById(patientId)).thenReturn(Optional.of(patient));
        when(turnRepo.existsByDoctor_IdAndScheduledAt(doctorId, scheduledAt)).thenReturn(false);
        when(turnRepo.save(any(TurnAssigned.class))).thenReturn(turnEntity);
        when(mapper.toDTO(turnEntity)).thenReturn(turnResponse);

        TurnResponseDTO result = turnAssignedService.createTurn(createRequest);

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
        when(userRepo.findById(doctorId)).thenReturn(Optional.empty());

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
        when(userRepo.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(userRepo.findById(patientId)).thenReturn(Optional.empty());

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
        when(userRepo.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(userRepo.findById(patientId)).thenReturn(Optional.of(patient));
        when(turnRepo.existsByDoctor_IdAndScheduledAt(doctorId, scheduledAt)).thenReturn(true);

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
    void createTurn_DoctorNotActive_ThrowsException() {
        doctor.setStatus("PENDING");
        when(userRepo.findById(doctorId)).thenReturn(Optional.of(doctor));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            turnAssignedService.createTurn(createRequest);
        });

        assertEquals("Doctor is not active", exception.getMessage());
        verify(userRepo).findById(doctorId);
        verify(userRepo, never()).findById(patientId);
        verify(turnRepo, never()).existsByDoctor_IdAndScheduledAt(any(), any());
        verify(turnRepo, never()).save(any());
    }

    @Test
    void createTurn_DoctorInvalidRole_ThrowsException() {
        doctor.setRole("PATIENT");
        when(userRepo.findById(doctorId)).thenReturn(Optional.of(doctor));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            turnAssignedService.createTurn(createRequest);
        });

        assertEquals("User is not a doctor", exception.getMessage());
        verify(userRepo).findById(doctorId);
        verify(userRepo, never()).findById(patientId);
        verify(turnRepo, never()).existsByDoctor_IdAndScheduledAt(any(), any());
        verify(turnRepo, never()).save(any());
    }

    @Test
    void createTurn_PatientNotActive_ThrowsException() {
        patient.setStatus("DISABLED");
        when(userRepo.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(userRepo.findById(patientId)).thenReturn(Optional.of(patient));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            turnAssignedService.createTurn(createRequest);
        });

        assertEquals("Patient is not active", exception.getMessage());
        verify(userRepo).findById(doctorId);
        verify(userRepo).findById(patientId);
        verify(turnRepo, never()).existsByDoctor_IdAndScheduledAt(any(), any());
        verify(turnRepo, never()).save(any());
    }

    @Test
    void createTurn_PatientInvalidRole_ThrowsException() {
        patient.setRole("ADMIN");
        when(userRepo.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(userRepo.findById(patientId)).thenReturn(Optional.of(patient));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            turnAssignedService.createTurn(createRequest);
        });

        assertEquals("User is not a patient", exception.getMessage());
        verify(userRepo).findById(doctorId);
        verify(userRepo).findById(patientId);
        verify(turnRepo, never()).existsByDoctor_IdAndScheduledAt(any(), any());
        verify(turnRepo, never()).save(any());
    }

    @Test
    void reserveTurn_Success() {
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

        TurnAssigned result = turnAssignedService.reserveTurn(turnId, patientId);

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
        when(turnRepo.findById(turnId)).thenReturn(Optional.empty());

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
        TurnAssigned scheduledTurn = TurnAssigned.builder()
                .id(turnId)
                .doctor(doctor)
                .patient(patient)
                .scheduledAt(scheduledAt)
                .status("SCHEDULED")
                .build();

        when(turnRepo.findById(turnId)).thenReturn(Optional.of(scheduledTurn));

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
        TurnAssigned availableTurn = TurnAssigned.builder()
                .id(turnId)
                .doctor(doctor)
                .patient(null)
                .scheduledAt(scheduledAt)
                .status("AVAILABLE")
                .build();

        when(turnRepo.findById(turnId)).thenReturn(Optional.of(availableTurn));
        when(userRepo.findById(patientId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            turnAssignedService.reserveTurn(turnId, patientId);
        });

        assertEquals("Patient not found", exception.getMessage());
        verify(turnRepo).findById(turnId);
        verify(userRepo).findById(patientId);
        verify(turnRepo, never()).save(any());
    }
    
    @Test
    void cancelTurn_Success() {
        TurnAssigned scheduledTurn = TurnAssigned.builder()
                .id(turnId)
                .doctor(doctor)
                .patient(patient)
                .scheduledAt(OffsetDateTime.now().plusDays(1)) 
                .status("SCHEDULED")
                .build();

        TurnAssigned canceledTurn = TurnAssigned.builder()
                .id(turnId)
                .doctor(doctor)
                .patient(patient)
                .scheduledAt(OffsetDateTime.now().plusDays(1))
                .status("CANCELED")
                .build();

        when(turnRepo.findById(turnId)).thenReturn(Optional.of(scheduledTurn));
        when(turnRepo.save(any(TurnAssigned.class))).thenReturn(canceledTurn);
        when(mapper.toDTO(canceledTurn)).thenReturn(turnResponse);

        TurnResponseDTO result = turnAssignedService.cancelTurn(turnId, patientId, "PATIENT");

        assertNotNull(result);
        verify(turnRepo).findById(turnId);
        verify(turnRepo).save(scheduledTurn);
        verify(mapper).toDTO(canceledTurn);
        assertEquals("CANCELED", scheduledTurn.getStatus());
    }

    @Test
    void cancelTurn_TurnNotFound_ThrowsException() {
        when(turnRepo.findById(turnId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            turnAssignedService.cancelTurn(turnId, patientId, "PATIENT");
        });

        assertEquals("Turn not found", exception.getMessage());
        verify(turnRepo).findById(turnId);
        verify(turnRepo, never()).save(any());
    }

    @Test
    void cancelTurn_NotPatientTurn_ThrowsException() {
        UUID differentPatientId = UUID.randomUUID();
        User differentPatient = new User();
        differentPatient.setId(differentPatientId);
        differentPatient.setEmail("other@example.com");
        differentPatient.setRole("PATIENT");

        TurnAssigned scheduledTurn = TurnAssigned.builder()
                .id(turnId)
                .doctor(doctor)
                .patient(differentPatient)
                .scheduledAt(OffsetDateTime.now().plusDays(1))
                .status("SCHEDULED")
                .build();

        when(turnRepo.findById(turnId)).thenReturn(Optional.of(scheduledTurn));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            turnAssignedService.cancelTurn(turnId, patientId, "PATIENT");
        });

        assertEquals("You can only cancel your own turns", exception.getMessage());
        verify(turnRepo).findById(turnId);
        verify(turnRepo, never()).save(any());
    }

    @Test
    void cancelTurn_TurnAlreadyCompleted_ThrowsException() {
        TurnAssigned completedTurn = TurnAssigned.builder()
                .id(turnId)
                .doctor(doctor)
                .patient(patient)
                .scheduledAt(OffsetDateTime.now().plusDays(1))
                .status("COMPLETED")
                .build();

        when(turnRepo.findById(turnId)).thenReturn(Optional.of(completedTurn));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            turnAssignedService.cancelTurn(turnId, patientId, "PATIENT");
        });

        assertEquals("Turn cannot be canceled. Current status: COMPLETED", exception.getMessage());
        verify(turnRepo).findById(turnId);
        verify(turnRepo, never()).save(any());
    }

    @Test
    void cancelTurn_PastTurn_ThrowsException() {
        TurnAssigned pastTurn = TurnAssigned.builder()
                .id(turnId)
                .doctor(doctor)
                .patient(patient)
                .scheduledAt(OffsetDateTime.now().minusDays(1)) 
                .status("SCHEDULED")
                .build();

        when(turnRepo.findById(turnId)).thenReturn(Optional.of(pastTurn));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            turnAssignedService.cancelTurn(turnId, patientId, "PATIENT");
        });

        assertEquals("Cannot cancel past turns", exception.getMessage());
        verify(turnRepo).findById(turnId);
        verify(turnRepo, never()).save(any());
    }

    @Test
    void cancelTurn_ReservedTurn_Success() {
        TurnAssigned reservedTurn = TurnAssigned.builder()
                .id(turnId)
                .doctor(doctor)
                .patient(patient)
                .scheduledAt(OffsetDateTime.now().plusDays(1))
                .status("RESERVED")
                .build();

        TurnAssigned canceledTurn = TurnAssigned.builder()
                .id(turnId)
                .doctor(doctor)
                .patient(patient)
                .scheduledAt(OffsetDateTime.now().plusDays(1))
                .status("CANCELED")
                .build();

        when(turnRepo.findById(turnId)).thenReturn(Optional.of(reservedTurn));
        when(turnRepo.save(any(TurnAssigned.class))).thenReturn(canceledTurn);
        when(mapper.toDTO(canceledTurn)).thenReturn(turnResponse);

        TurnResponseDTO result = turnAssignedService.cancelTurn(turnId, patientId, "PATIENT");

        assertNotNull(result);
        verify(turnRepo).findById(turnId);
        verify(turnRepo).save(reservedTurn);
        verify(mapper).toDTO(canceledTurn);
        assertEquals("CANCELED", reservedTurn.getStatus());
    }
    
    @Test
    void cancelTurn_DoctorCancelOwnTurn_Success() {
        TurnAssigned scheduledTurn = TurnAssigned.builder()
                .id(turnId)
                .doctor(doctor)
                .patient(patient)
                .scheduledAt(OffsetDateTime.now().plusDays(1))
                .status("SCHEDULED")
                .build();

        TurnAssigned canceledTurn = TurnAssigned.builder()
                .id(turnId)
                .doctor(doctor)
                .patient(patient)
                .scheduledAt(OffsetDateTime.now().plusDays(1))
                .status("CANCELED")
                .build();

        when(turnRepo.findById(turnId)).thenReturn(Optional.of(scheduledTurn));
        when(turnRepo.save(any(TurnAssigned.class))).thenReturn(canceledTurn);
        when(mapper.toDTO(canceledTurn)).thenReturn(turnResponse);

        TurnResponseDTO result = turnAssignedService.cancelTurn(turnId, doctorId, "DOCTOR");

        assertNotNull(result);
        verify(turnRepo).findById(turnId);
        verify(turnRepo).save(scheduledTurn);
        verify(mapper).toDTO(canceledTurn);
        assertEquals("CANCELED", scheduledTurn.getStatus());
    }

    @Test
    void cancelTurn_DoctorCannotCancelOtherDoctorTurn_ThrowsException() {
        UUID differentDoctorId = UUID.randomUUID();
        User differentDoctor = new User();
        differentDoctor.setId(differentDoctorId);
        differentDoctor.setEmail("other@doctor.com");
        differentDoctor.setRole("DOCTOR");

        TurnAssigned scheduledTurn = TurnAssigned.builder()
                .id(turnId)
                .doctor(differentDoctor)
                .patient(patient)
                .scheduledAt(OffsetDateTime.now().plusDays(1))
                .status("SCHEDULED")
                .build();

        when(turnRepo.findById(turnId)).thenReturn(Optional.of(scheduledTurn));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            turnAssignedService.cancelTurn(turnId, doctorId, "DOCTOR");
        });

        assertEquals("You can only cancel your own turns", exception.getMessage());
        verify(turnRepo).findById(turnId);
        verify(turnRepo, never()).save(any());
    }

    @Test
    void cancelTurn_InvalidRole_ThrowsException() {
        TurnAssigned scheduledTurn = TurnAssigned.builder()
                .id(turnId)
                .doctor(doctor)
                .patient(patient)
                .scheduledAt(OffsetDateTime.now().plusDays(1))
                .status("SCHEDULED")
                .build();

        when(turnRepo.findById(turnId)).thenReturn(Optional.of(scheduledTurn));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            turnAssignedService.cancelTurn(turnId, patientId, "ADMIN");
        });

        assertEquals("Invalid user role for cancellation", exception.getMessage());
        verify(turnRepo).findById(turnId);
        verify(turnRepo, never()).save(any());
    }

    @Test
    void getTurnsByDoctor_Success() {
        List<TurnAssigned> turns = Arrays.asList(turnEntity);
        List<TurnResponseDTO> expectedResponse = Arrays.asList(turnResponse);

        when(turnRepo.findByDoctor_IdOrderByScheduledAtDesc(doctorId)).thenReturn(turns);
        when(mapper.toDTO(turnEntity)).thenReturn(turnResponse);

        List<TurnResponseDTO> result = turnAssignedService.getTurnsByDoctor(doctorId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedResponse.get(0).getId(), result.get(0).getId());
        verify(turnRepo).findByDoctor_IdOrderByScheduledAtDesc(doctorId);
        verify(mapper).toDTO(turnEntity);
    }

    @Test
    void getTurnsByPatient_Success() {
        List<TurnAssigned> turns = Arrays.asList(turnEntity);
        List<TurnResponseDTO> expectedResponse = Arrays.asList(turnResponse);

        when(turnRepo.findByPatient_IdOrderByScheduledAtDesc(patientId)).thenReturn(turns);
        when(mapper.toDTO(turnEntity)).thenReturn(turnResponse);

        List<TurnResponseDTO> result = turnAssignedService.getTurnsByPatient(patientId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedResponse.get(0).getId(), result.get(0).getId());
        verify(turnRepo).findByPatient_IdOrderByScheduledAtDesc(patientId);
        verify(mapper).toDTO(turnEntity);
    }

    @Test
    void getTurnsByDoctorAndStatus_Success() {
        String status = "SCHEDULED";
        List<TurnAssigned> turns = Arrays.asList(turnEntity);
        List<TurnResponseDTO> expectedResponse = Arrays.asList(turnResponse);

        when(turnRepo.findByDoctor_IdAndStatusOrderByScheduledAtDesc(doctorId, status)).thenReturn(turns);
        when(mapper.toDTO(turnEntity)).thenReturn(turnResponse);

        List<TurnResponseDTO> result = turnAssignedService.getTurnsByDoctorAndStatus(doctorId, status);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedResponse.get(0).getId(), result.get(0).getId());
        verify(turnRepo).findByDoctor_IdAndStatusOrderByScheduledAtDesc(doctorId, status);
        verify(mapper).toDTO(turnEntity);
    }

    @Test
    void getTurnsByPatientAndStatus_Success() {
        String status = "SCHEDULED";
        List<TurnAssigned> turns = Arrays.asList(turnEntity);
        List<TurnResponseDTO> expectedResponse = Arrays.asList(turnResponse);

        when(turnRepo.findByPatient_IdAndStatusOrderByScheduledAtDesc(patientId, status)).thenReturn(turns);
        when(mapper.toDTO(turnEntity)).thenReturn(turnResponse);

        List<TurnResponseDTO> result = turnAssignedService.getTurnsByPatientAndStatus(patientId, status);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedResponse.get(0).getId(), result.get(0).getId());
        verify(turnRepo).findByPatient_IdAndStatusOrderByScheduledAtDesc(patientId, status);
        verify(mapper).toDTO(turnEntity);
    }

    @Test
    void cancelTurn_PatientRoleWithNullPatient_ThrowsException() {
        TurnAssigned turnWithNullPatient = TurnAssigned.builder()
                .id(turnId)
                .doctor(doctor)
                .patient(null)
                .scheduledAt(OffsetDateTime.now().plusDays(1))
                .status("SCHEDULED")
                .build();

        when(turnRepo.findById(turnId)).thenReturn(Optional.of(turnWithNullPatient));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            turnAssignedService.cancelTurn(turnId, patientId, "PATIENT");
        });

        assertEquals("You can only cancel your own turns", exception.getMessage());
        verify(turnRepo).findById(turnId);
        verify(turnRepo, never()).save(any());
    }

    @Test
    void cancelTurn_DoctorRoleWithNullDoctor_ThrowsException() {
        TurnAssigned turnWithNullDoctor = TurnAssigned.builder()
                .id(turnId)
                .doctor(null)
                .patient(patient)
                .scheduledAt(OffsetDateTime.now().plusDays(1))
                .status("SCHEDULED")
                .build();

        when(turnRepo.findById(turnId)).thenReturn(Optional.of(turnWithNullDoctor));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            turnAssignedService.cancelTurn(turnId, doctorId, "DOCTOR");
        });

        assertEquals("You can only cancel your own turns", exception.getMessage());
        verify(turnRepo).findById(turnId);
        verify(turnRepo, never()).save(any());
    }

    @Test
    void cancelTurn_WithPendingModifyRequest_DeletesRequest() {
        TurnAssigned scheduledTurn = TurnAssigned.builder()
                .id(turnId)
                .doctor(doctor)
                .patient(patient)
                .scheduledAt(OffsetDateTime.now().plusDays(1))
                .status("SCHEDULED")
                .build();

        TurnAssigned canceledTurn = TurnAssigned.builder()
                .id(turnId)
                .doctor(doctor)
                .patient(patient)
                .scheduledAt(OffsetDateTime.now().plusDays(1))
                .status("CANCELED")
                .build();

        // Simular que existe una solicitud PENDING
        com.medibook.api.entity.TurnModifyRequest pendingRequest = new com.medibook.api.entity.TurnModifyRequest();
        pendingRequest.setId(UUID.randomUUID());
        when(turnRepo.findById(turnId)).thenReturn(Optional.of(scheduledTurn));
        when(turnRepo.save(any(TurnAssigned.class))).thenReturn(canceledTurn);
        when(mapper.toDTO(canceledTurn)).thenReturn(turnResponse);
        when(turnModifyRequestRepository.findByTurnAssigned_IdAndStatus(turnId, "PENDING"))
            .thenReturn(Optional.of(pendingRequest));

        TurnResponseDTO result = turnAssignedService.cancelTurn(turnId, patientId, "PATIENT");

        assertNotNull(result);
        verify(turnRepo).findById(turnId);
        verify(turnRepo).save(scheduledTurn);
        verify(mapper).toDTO(canceledTurn);
        verify(turnModifyRequestRepository).findByTurnAssigned_IdAndStatus(turnId, "PENDING");
        verify(turnModifyRequestRepository).deleteByTurnAssigned_IdAndStatus(turnId, "PENDING");
        assertEquals("CANCELED", scheduledTurn.getStatus());
    }
    
    @Test
    void cancelTurn_NoPendingModifyRequest_DoesNotDeleteRequest() {
        TurnAssigned scheduledTurn = TurnAssigned.builder()
                .id(turnId)
                .doctor(doctor)
                .patient(patient)
                .scheduledAt(OffsetDateTime.now().plusDays(1))
                .status("SCHEDULED")
                .build();

        TurnAssigned canceledTurn = TurnAssigned.builder()
                .id(turnId)
                .doctor(doctor)
                .patient(patient)
                .scheduledAt(OffsetDateTime.now().plusDays(1))
                .status("CANCELED")
                .build();

        when(turnRepo.findById(turnId)).thenReturn(Optional.of(scheduledTurn));
        when(turnRepo.save(any(TurnAssigned.class))).thenReturn(canceledTurn);
        when(mapper.toDTO(canceledTurn)).thenReturn(turnResponse);
        when(turnModifyRequestRepository.findByTurnAssigned_IdAndStatus(turnId, "PENDING"))
                .thenReturn(Optional.empty());

        TurnResponseDTO result = turnAssignedService.cancelTurn(turnId, patientId, "PATIENT");

        assertNotNull(result);
        verify(turnRepo).findById(turnId);
        verify(turnRepo).save(scheduledTurn);
        verify(mapper).toDTO(canceledTurn);
        verify(turnModifyRequestRepository).findByTurnAssigned_IdAndStatus(turnId, "PENDING");
        verify(turnModifyRequestRepository, never()).deleteByTurnAssigned_IdAndStatus(any(UUID.class), eq("PENDING"));
        assertEquals("CANCELED", scheduledTurn.getStatus());
    }
}
