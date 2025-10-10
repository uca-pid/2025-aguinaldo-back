package com.medibook.api.service;

import com.medibook.api.dto.MedicalHistoryDTO;
import com.medibook.api.dto.PatientDTO;
import com.medibook.api.entity.TurnAssigned;
import com.medibook.api.entity.User;
import com.medibook.api.mapper.DoctorMapper;
import com.medibook.api.repository.TurnAssignedRepository;
import com.medibook.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private TurnAssignedRepository turnAssignedRepository;
    
    @Mock
    private DoctorMapper doctorMapper;

    @Mock
    private MedicalHistoryService medicalHistoryService;

    @InjectMocks
    private DoctorService doctorService;

    private UUID doctorId;
    private UUID patientId1;
    private UUID patientId2;
    private UUID turnId;
    private TurnAssigned turn;
    private User doctor;
    private User patient1;
    private User patient2;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();
        patientId1 = UUID.randomUUID();
        patientId2 = UUID.randomUUID();
    turnId = UUID.randomUUID();

        doctor = new User();
        doctor.setId(doctorId);
        doctor.setName("Dr. John");
        doctor.setSurname("Smith");
        doctor.setEmail("doctor@example.com");
        doctor.setRole("DOCTOR");
        doctor.setStatus("ACTIVE");

        patient1 = new User();
        patient1.setId(patientId1);
        patient1.setName("Patient");
        patient1.setSurname("One");
        patient1.setEmail("patient1@example.com");
        patient1.setDni(12345678L);
        patient1.setPhone("555-1234");
        patient1.setBirthdate(LocalDate.of(1990, 1, 1));
        patient1.setGender("MALE");
        patient1.setRole("PATIENT");
        patient1.setStatus("ACTIVE");

        patient2 = new User();
        patient2.setId(patientId2);
        patient2.setName("Patient");
        patient2.setSurname("Two");
        patient2.setEmail("patient2@example.com");
        patient2.setDni(87654321L);
        patient2.setPhone("555-5678");
        patient2.setBirthdate(LocalDate.of(1985, 5, 15));
        patient2.setGender("FEMALE");
        patient2.setRole("PATIENT");
        patient2.setStatus("ACTIVE");

    turn = TurnAssigned.builder()
        .id(turnId)
        .doctor(doctor)
        .patient(patient1)
        .status("COMPLETED")
        .build();
    }

    @Test
    void getPatientsByDoctor_Success() {
        List<User> patients = Arrays.asList(patient1, patient2);
        
        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(turnAssignedRepository.findDistinctPatientsByDoctorId(doctorId)).thenReturn(patients);
        when(medicalHistoryService.getPatientMedicalHistory(patientId1)).thenReturn(Collections.emptyList());
        when(medicalHistoryService.getPatientMedicalHistory(patientId2)).thenReturn(Collections.emptyList());
        when(medicalHistoryService.getLatestMedicalHistoryContent(patientId1)).thenReturn(null);
        when(medicalHistoryService.getLatestMedicalHistoryContent(patientId2)).thenReturn(null);

        List<PatientDTO> result = doctorService.getPatientsByDoctor(doctorId);

        assertEquals(2, result.size());
        
        PatientDTO patientDTO1 = result.get(0);
        assertEquals(patientId1, patientDTO1.getId());
        assertEquals("Patient", patientDTO1.getName());
        assertEquals("One", patientDTO1.getSurname());
        assertEquals("patient1@example.com", patientDTO1.getEmail());
        assertEquals(12345678L, patientDTO1.getDni());
        assertEquals("555-1234", patientDTO1.getPhone());
        assertEquals(LocalDate.of(1990, 1, 1), patientDTO1.getBirthdate());
        assertEquals("MALE", patientDTO1.getGender());
        assertEquals("ACTIVE", patientDTO1.getStatus());
        assertNotNull(patientDTO1.getMedicalHistories());
        assertTrue(patientDTO1.getMedicalHistories().isEmpty());

        PatientDTO patientDTO2 = result.get(1);
        assertEquals(patientId2, patientDTO2.getId());
        assertEquals("Patient", patientDTO2.getName());
        assertEquals("Two", patientDTO2.getSurname());
        assertEquals("patient2@example.com", patientDTO2.getEmail());
        assertEquals(87654321L, patientDTO2.getDni());
        assertEquals("555-5678", patientDTO2.getPhone());
        assertEquals(LocalDate.of(1985, 5, 15), patientDTO2.getBirthdate());
        assertEquals("FEMALE", patientDTO2.getGender());
        assertEquals("ACTIVE", patientDTO2.getStatus());
        assertNotNull(patientDTO2.getMedicalHistories());
        assertTrue(patientDTO2.getMedicalHistories().isEmpty());

        verify(userRepository).findById(doctorId);
        verify(turnAssignedRepository).findDistinctPatientsByDoctorId(doctorId);
        verify(medicalHistoryService).getPatientMedicalHistory(patientId1);
        verify(medicalHistoryService).getPatientMedicalHistory(patientId2);
        verify(medicalHistoryService).getLatestMedicalHistoryContent(patientId1);
        verify(medicalHistoryService).getLatestMedicalHistoryContent(patientId2);
    }

    @Test
    void getPatientsByDoctor_DoctorNotFound() {
        when(userRepository.findById(doctorId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> doctorService.getPatientsByDoctor(doctorId));
        
        assertEquals("Doctor not found", exception.getMessage());
        verify(userRepository).findById(doctorId);
        verifyNoInteractions(turnAssignedRepository);
    }

    @Test
    void getPatientsByDoctor_DoctorNotActiveRole() {
        doctor.setRole("PATIENT");
        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> doctorService.getPatientsByDoctor(doctorId));
        
        assertEquals("Invalid doctor or doctor is not active", exception.getMessage());
        verify(userRepository).findById(doctorId);
        verifyNoInteractions(turnAssignedRepository);
    }

    @Test
    void getPatientsByDoctor_DoctorNotActiveStatus() {
        doctor.setStatus("PENDING");
        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> doctorService.getPatientsByDoctor(doctorId));
        
        assertEquals("Invalid doctor or doctor is not active", exception.getMessage());
        verify(userRepository).findById(doctorId);
        verifyNoInteractions(turnAssignedRepository);
    }

    @Test
    void getPatientsByDoctor_EmptyPatientList() {
        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(turnAssignedRepository.findDistinctPatientsByDoctorId(doctorId)).thenReturn(Collections.emptyList());

        List<PatientDTO> result = doctorService.getPatientsByDoctor(doctorId);

        assertTrue(result.isEmpty());
        verify(userRepository).findById(doctorId);
        verify(turnAssignedRepository).findDistinctPatientsByDoctorId(doctorId);
        verifyNoInteractions(medicalHistoryService);
    }

    @Test
    void updatePatientMedicalHistory_Success() {
        String medicalHistory = "Patient has diabetes";
        MedicalHistoryDTO expectedResult = MedicalHistoryDTO.builder()
                .id(UUID.randomUUID())
                .content(medicalHistory)
                .patientId(patientId1)
                .doctorId(doctorId)
                .build();

        when(turnAssignedRepository.findById(turnId)).thenReturn(Optional.of(turn));
        when(medicalHistoryService.addMedicalHistory(doctorId, turnId, medicalHistory))
                .thenReturn(expectedResult);

        assertDoesNotThrow(() -> 
            doctorService.updatePatientMedicalHistory(doctorId, patientId1, turnId, medicalHistory));

        verify(turnAssignedRepository).findById(turnId);
        verify(medicalHistoryService).addMedicalHistory(doctorId, turnId, medicalHistory);
    }

    @Test
    void updatePatientMedicalHistory_TurnNotFound() {
    when(turnAssignedRepository.findById(turnId)).thenReturn(Optional.empty());

    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> doctorService.updatePatientMedicalHistory(doctorId, patientId1, turnId, "notes"));

    assertEquals("Turn not found", exception.getMessage());
    verify(turnAssignedRepository).findById(turnId);
    verifyNoInteractions(medicalHistoryService);
    }

    @Test
    void updatePatientMedicalHistory_TurnBelongsToDifferentDoctor() {
    User otherDoctor = new User();
    otherDoctor.setId(UUID.randomUUID());
    otherDoctor.setRole("DOCTOR");
    otherDoctor.setStatus("ACTIVE");

    TurnAssigned foreignTurn = TurnAssigned.builder()
        .id(turnId)
        .doctor(otherDoctor)
        .patient(patient1)
        .status("COMPLETED")
        .build();

    when(turnAssignedRepository.findById(turnId)).thenReturn(Optional.of(foreignTurn));

    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> doctorService.updatePatientMedicalHistory(doctorId, patientId1, turnId, "notes"));

    assertEquals("Doctor can only update medical history for their own turns", exception.getMessage());
    verify(turnAssignedRepository).findById(turnId);
    verifyNoInteractions(medicalHistoryService);
    }

    @Test
    void updatePatientMedicalHistory_TurnBelongsToDifferentPatient() {
    TurnAssigned foreignPatientTurn = TurnAssigned.builder()
        .id(turnId)
        .doctor(doctor)
        .patient(patient2)
        .status("COMPLETED")
        .build();

    when(turnAssignedRepository.findById(turnId)).thenReturn(Optional.of(foreignPatientTurn));

    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> doctorService.updatePatientMedicalHistory(doctorId, patientId1, turnId, "notes"));

    assertEquals("Turn does not belong to the specified patient", exception.getMessage());
    verify(turnAssignedRepository).findById(turnId);
    verifyNoInteractions(medicalHistoryService);
    }
}