package com.medibook.api.service;

import com.medibook.api.dto.Turn.TurnModifyRequestDTO;
import com.medibook.api.dto.Turn.TurnModifyRequestResponseDTO;
import com.medibook.api.entity.TurnAssigned;
import com.medibook.api.entity.TurnModifyRequest;
import com.medibook.api.entity.User;
import com.medibook.api.mapper.TurnModifyRequestMapper;
import com.medibook.api.repository.TurnAssignedRepository;
import com.medibook.api.repository.TurnModifyRequestRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TurnModifyRequestServiceTest {

    @Mock
    private TurnModifyRequestRepository turnModifyRequestRepository;
    
    @Mock
    private TurnAssignedRepository turnAssignedRepository;
    
    @Mock
    private TurnModifyRequestMapper mapper;
    
    @Mock
    private com.medibook.api.service.email.EmailService emailService;
    
    @InjectMocks
    private TurnModifyRequestService service;
    
    private User patient;
    private User doctor;
    private TurnAssigned turnAssigned;
    private TurnModifyRequestDTO requestDTO;
    private TurnModifyRequest modifyRequest;
    private TurnModifyRequestResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        patient = new User();
        patient.setId(UUID.randomUUID());
        
        doctor = new User();
        doctor.setId(UUID.randomUUID());
        
        turnAssigned = new TurnAssigned();
        turnAssigned.setId(UUID.randomUUID());
        turnAssigned.setPatient(patient);
        turnAssigned.setDoctor(doctor);
        turnAssigned.setScheduledAt(OffsetDateTime.now().plusDays(1));
        
        requestDTO = new TurnModifyRequestDTO();
        requestDTO.setTurnId(turnAssigned.getId());
        requestDTO.setNewScheduledAt(OffsetDateTime.now().plusDays(2));
        
        modifyRequest = TurnModifyRequest.builder()
                .id(UUID.randomUUID())
                .turnAssigned(turnAssigned)
                .patient(patient)
                .doctor(doctor)
                .currentScheduledAt(turnAssigned.getScheduledAt())
                .requestedScheduledAt(requestDTO.getNewScheduledAt())
                .status("PENDING")
                .build();
        
        responseDTO = new TurnModifyRequestResponseDTO();
        responseDTO.setId(modifyRequest.getId());
        responseDTO.setStatus("PENDING");
    }

    @Test
    void createModifyRequest_WithValidRequest_ShouldCreateSuccessfully() {
        when(turnAssignedRepository.findById(requestDTO.getTurnId())).thenReturn(Optional.of(turnAssigned));
        when(turnModifyRequestRepository.findPendingRequestByTurnAndPatient(any(), any())).thenReturn(Optional.empty());
        when(turnModifyRequestRepository.save(any(TurnModifyRequest.class))).thenReturn(modifyRequest);
        when(mapper.toResponseDTO(modifyRequest)).thenReturn(responseDTO);
        
        TurnModifyRequestResponseDTO result = service.createModifyRequest(requestDTO, patient);
        
        assertNotNull(result);
        assertEquals(modifyRequest.getId(), result.getId());
        verify(turnAssignedRepository).findById(requestDTO.getTurnId());
        verify(turnModifyRequestRepository).save(any(TurnModifyRequest.class));
        verify(mapper).toResponseDTO(modifyRequest);
    }

    @Test
    void createModifyRequest_WithNonExistentTurn_ShouldThrowException() {
        when(turnAssignedRepository.findById(requestDTO.getTurnId())).thenReturn(Optional.empty());
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.createModifyRequest(requestDTO, patient));
        assertEquals("Turn not found", exception.getMessage());
        
        verify(turnAssignedRepository).findById(requestDTO.getTurnId());
        verifyNoInteractions(turnModifyRequestRepository);
    }

    @Test
    void createModifyRequest_WithWrongPatient_ShouldThrowException() {
        User otherPatient = new User();
        otherPatient.setId(UUID.randomUUID());
        
        when(turnAssignedRepository.findById(requestDTO.getTurnId())).thenReturn(Optional.of(turnAssigned));
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.createModifyRequest(requestDTO, otherPatient));
        assertEquals("Turn does not belong to this patient", exception.getMessage());
        
        verify(turnAssignedRepository).findById(requestDTO.getTurnId());
        verifyNoMoreInteractions(turnModifyRequestRepository);
    }

    @Test
    void createModifyRequest_WithPastTurn_ShouldThrowException() {
        turnAssigned.setScheduledAt(OffsetDateTime.now().minusDays(1));
        when(turnAssignedRepository.findById(requestDTO.getTurnId())).thenReturn(Optional.of(turnAssigned));
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.createModifyRequest(requestDTO, patient));
        assertEquals("Cannot modify past appointments", exception.getMessage());
        
        verify(turnAssignedRepository).findById(requestDTO.getTurnId());
        verifyNoMoreInteractions(turnModifyRequestRepository);
    }

    @Test
    void createModifyRequest_WithPastNewScheduledAt_ShouldThrowException() {
        requestDTO.setNewScheduledAt(OffsetDateTime.now().minusDays(1));
        when(turnAssignedRepository.findById(requestDTO.getTurnId())).thenReturn(Optional.of(turnAssigned));
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.createModifyRequest(requestDTO, patient));
        assertEquals("Cannot schedule appointments in the past", exception.getMessage());
        
        verify(turnAssignedRepository).findById(requestDTO.getTurnId());
        verifyNoMoreInteractions(turnModifyRequestRepository);
    }

    @Test
    void createModifyRequest_WithExistingPendingRequest_ShouldThrowException() {
        when(turnAssignedRepository.findById(requestDTO.getTurnId())).thenReturn(Optional.of(turnAssigned));
        when(turnModifyRequestRepository.findPendingRequestByTurnAndPatient(any(), any()))
                .thenReturn(Optional.of(modifyRequest));
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.createModifyRequest(requestDTO, patient));
        assertEquals("There is already a pending modification request for this appointment", exception.getMessage());
        
        verify(turnAssignedRepository).findById(requestDTO.getTurnId());
        verify(turnModifyRequestRepository).findPendingRequestByTurnAndPatient(requestDTO.getTurnId(), patient.getId());
        verify(turnModifyRequestRepository, never()).save(any());
    }

    @Test
    void getPatientRequests_ShouldReturnMappedRequests() {
        List<TurnModifyRequest> requests = Arrays.asList(modifyRequest);
        when(turnModifyRequestRepository.findByPatient_IdOrderByIdDesc(patient.getId())).thenReturn(requests);
        when(mapper.toResponseDTO(modifyRequest)).thenReturn(responseDTO);
        
        List<TurnModifyRequestResponseDTO> result = service.getPatientRequests(patient.getId());
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(responseDTO, result.get(0));
        verify(turnModifyRequestRepository).findByPatient_IdOrderByIdDesc(patient.getId());
        verify(mapper).toResponseDTO(modifyRequest);
    }

    @Test
    void getDoctorPendingRequests_ShouldReturnMappedRequests() {
        List<TurnModifyRequest> requests = Arrays.asList(modifyRequest);
        when(turnModifyRequestRepository.findByDoctor_IdAndStatusOrderByIdDesc(doctor.getId(), "PENDING"))
                .thenReturn(requests);
        when(mapper.toResponseDTO(modifyRequest)).thenReturn(responseDTO);
        
        List<TurnModifyRequestResponseDTO> result = service.getDoctorPendingRequests(doctor.getId());
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(responseDTO, result.get(0));
        verify(turnModifyRequestRepository).findByDoctor_IdAndStatusOrderByIdDesc(doctor.getId(), "PENDING");
        verify(mapper).toResponseDTO(modifyRequest);
    }
}