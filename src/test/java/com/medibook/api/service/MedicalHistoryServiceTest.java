package com.medibook.api.service;

import com.medibook.api.dto.MedicalHistoryDTO;
import com.medibook.api.entity.MedicalHistory;
import com.medibook.api.entity.User;
import com.medibook.api.repository.MedicalHistoryRepository;
import com.medibook.api.repository.TurnAssignedRepository;
import com.medibook.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalHistoryServiceTest {

    @Mock
    private MedicalHistoryRepository medicalHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TurnAssignedRepository turnAssignedRepository;

    @InjectMocks
    private MedicalHistoryService medicalHistoryService;

    private User doctor;
    private User patient;
    private MedicalHistory medicalHistory;
    private UUID doctorId;
    private UUID patientId;
    private UUID historyId;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        historyId = UUID.randomUUID();

        doctor = new User();
        doctor.setId(doctorId);
        doctor.setName("Dr. John");
        doctor.setSurname("Smith");
        doctor.setRole("DOCTOR");
        doctor.setStatus("ACTIVE");

        patient = new User();
        patient.setId(patientId);
        patient.setName("Patient");
        patient.setSurname("One");
        patient.setRole("PATIENT");
        patient.setStatus("ACTIVE");

        medicalHistory = new MedicalHistory();
        medicalHistory.setId(historyId);
        medicalHistory.setDoctor(doctor);
        medicalHistory.setPatient(patient);
        medicalHistory.setContent("Test medical history content");
        medicalHistory.setCreatedAt(LocalDateTime.now());
        medicalHistory.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void addMedicalHistory_Success() {
        String content = "New medical history entry";

        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(turnAssignedRepository.existsByDoctor_IdAndPatient_Id(doctorId, patientId)).thenReturn(true);
        when(medicalHistoryRepository.save(any(MedicalHistory.class))).thenReturn(medicalHistory);

        MedicalHistoryDTO result = medicalHistoryService.addMedicalHistory(doctorId, patientId, content);

        assertNotNull(result);
        assertEquals(historyId, result.getId());
        assertEquals(doctorId, result.getDoctorId());
        assertEquals(patientId, result.getPatientId());
        assertEquals("Dr. John", result.getDoctorName());
        assertEquals("Smith", result.getDoctorSurname());
        assertEquals("Patient", result.getPatientName());
        assertEquals("One", result.getPatientSurname());

        verify(userRepository).findById(doctorId);
        verify(userRepository).findById(patientId);
        verify(turnAssignedRepository).existsByDoctor_IdAndPatient_Id(doctorId, patientId);
        verify(medicalHistoryRepository).save(any(MedicalHistory.class));
    }

    @Test
    void addMedicalHistory_DoctorNotFound() {
        when(userRepository.findById(doctorId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> medicalHistoryService.addMedicalHistory(doctorId, patientId, "content"));

        assertEquals("Doctor not found", exception.getMessage());
        verify(userRepository).findById(doctorId);
        verifyNoInteractions(turnAssignedRepository, medicalHistoryRepository);
    }

    @Test
    void addMedicalHistory_PatientNotFound() {
        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(userRepository.findById(patientId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> medicalHistoryService.addMedicalHistory(doctorId, patientId, "content"));

        assertEquals("Patient not found", exception.getMessage());
        verify(userRepository).findById(doctorId);
        verify(userRepository).findById(patientId);
        verifyNoInteractions(turnAssignedRepository, medicalHistoryRepository);
    }

    @Test
    void addMedicalHistory_NoAppointmentHistory() {
        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(turnAssignedRepository.existsByDoctor_IdAndPatient_Id(doctorId, patientId)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> medicalHistoryService.addMedicalHistory(doctorId, patientId, "content"));

        assertEquals("Doctor can only add medical history for patients they have treated", exception.getMessage());
        verify(userRepository).findById(doctorId);
        verify(userRepository).findById(patientId);
        verify(turnAssignedRepository).existsByDoctor_IdAndPatient_Id(doctorId, patientId);
        verifyNoInteractions(medicalHistoryRepository);
    }

    @Test
    void getPatientMedicalHistory_Success() {
        List<MedicalHistory> histories = Arrays.asList(medicalHistory);
        when(medicalHistoryRepository.findByPatient_IdOrderByCreatedAtDesc(patientId)).thenReturn(histories);

        List<MedicalHistoryDTO> result = medicalHistoryService.getPatientMedicalHistory(patientId);

        assertEquals(1, result.size());
        MedicalHistoryDTO dto = result.get(0);
        assertEquals(historyId, dto.getId());
        assertEquals("Test medical history content", dto.getContent());

        verify(medicalHistoryRepository).findByPatient_IdOrderByCreatedAtDesc(patientId);
    }

    @Test
    void updateMedicalHistory_Success() {
        String newContent = "Updated medical history content";
        medicalHistory.setContent(newContent);

        when(medicalHistoryRepository.findById(historyId)).thenReturn(Optional.of(medicalHistory));
        when(medicalHistoryRepository.save(any(MedicalHistory.class))).thenReturn(medicalHistory);

        MedicalHistoryDTO result = medicalHistoryService.updateMedicalHistory(doctorId, historyId, newContent);

        assertNotNull(result);
        assertEquals(historyId, result.getId());
        assertEquals(newContent, result.getContent());

        verify(medicalHistoryRepository).findById(historyId);
        verify(medicalHistoryRepository).save(medicalHistory);
    }

    @Test
    void updateMedicalHistory_NotFound() {
        when(medicalHistoryRepository.findById(historyId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> medicalHistoryService.updateMedicalHistory(doctorId, historyId, "content"));

        assertEquals("Medical history entry not found", exception.getMessage());
        verify(medicalHistoryRepository).findById(historyId);
        verify(medicalHistoryRepository, never()).save(any());
    }

    @Test
    void updateMedicalHistory_WrongDoctor() {
        UUID wrongDoctorId = UUID.randomUUID();
        when(medicalHistoryRepository.findById(historyId)).thenReturn(Optional.of(medicalHistory));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> medicalHistoryService.updateMedicalHistory(wrongDoctorId, historyId, "content"));

        assertEquals("Doctor can only update their own medical history entries", exception.getMessage());
        verify(medicalHistoryRepository).findById(historyId);
        verify(medicalHistoryRepository, never()).save(any());
    }

    @Test
    void getDoctorMedicalHistoryEntries_Success() {
        List<MedicalHistory> histories = Arrays.asList(medicalHistory);
        when(medicalHistoryRepository.findByDoctor_IdOrderByCreatedAtDesc(doctorId)).thenReturn(histories);

        List<MedicalHistoryDTO> result = medicalHistoryService.getDoctorMedicalHistoryEntries(doctorId);

        assertEquals(1, result.size());
        MedicalHistoryDTO dto = result.get(0);
        assertEquals(historyId, dto.getId());
        assertEquals("Test medical history content", dto.getContent());

        verify(medicalHistoryRepository).findByDoctor_IdOrderByCreatedAtDesc(doctorId);
    }

    @Test
    void getPatientMedicalHistoryByDoctor_Success() {
        List<MedicalHistory> histories = Arrays.asList(medicalHistory);
        when(medicalHistoryRepository.findByPatient_IdAndDoctor_IdOrderByCreatedAtDesc(patientId, doctorId)).thenReturn(histories);

        List<MedicalHistoryDTO> result = medicalHistoryService.getPatientMedicalHistoryByDoctor(patientId, doctorId);

        assertEquals(1, result.size());
        MedicalHistoryDTO dto = result.get(0);
        assertEquals(historyId, dto.getId());
        assertEquals("Test medical history content", dto.getContent());

        verify(medicalHistoryRepository).findByPatient_IdAndDoctor_IdOrderByCreatedAtDesc(patientId, doctorId);
    }

    @Test
    void getLatestMedicalHistoryContent_Success() {
        when(medicalHistoryRepository.findLatestByPatientId(patientId)).thenReturn(medicalHistory);

        String result = medicalHistoryService.getLatestMedicalHistoryContent(patientId);

        assertEquals("Test medical history content", result);
        verify(medicalHistoryRepository).findLatestByPatientId(patientId);
    }

    @Test
    void getLatestMedicalHistoryContent_NoHistory() {
        when(medicalHistoryRepository.findLatestByPatientId(patientId)).thenReturn(null);

        String result = medicalHistoryService.getLatestMedicalHistoryContent(patientId);

        assertNull(result);
        verify(medicalHistoryRepository).findLatestByPatientId(patientId);
    }

    @Test
    void deleteMedicalHistory_Success() {
        when(medicalHistoryRepository.findById(historyId)).thenReturn(Optional.of(medicalHistory));

        assertDoesNotThrow(() -> medicalHistoryService.deleteMedicalHistory(doctorId, historyId));

        verify(medicalHistoryRepository).findById(historyId);
        verify(medicalHistoryRepository).delete(medicalHistory);
    }

    @Test
    void deleteMedicalHistory_NotFound() {
        when(medicalHistoryRepository.findById(historyId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> medicalHistoryService.deleteMedicalHistory(doctorId, historyId));

        assertEquals("Medical history entry not found", exception.getMessage());
        verify(medicalHistoryRepository).findById(historyId);
        verify(medicalHistoryRepository, never()).delete(any());
    }

    @Test
    void deleteMedicalHistory_WrongDoctor() {
        UUID wrongDoctorId = UUID.randomUUID();
        when(medicalHistoryRepository.findById(historyId)).thenReturn(Optional.of(medicalHistory));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> medicalHistoryService.deleteMedicalHistory(wrongDoctorId, historyId));

        assertEquals("Doctor can only delete their own medical history entries", exception.getMessage());
        verify(medicalHistoryRepository).findById(historyId);
        verify(medicalHistoryRepository, never()).delete(any());
    }

    @Test
    void addMedicalHistory_DoctorNotFound_ShouldThrowException() {
        when(userRepository.findById(doctorId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> medicalHistoryService.addMedicalHistory(doctorId, patientId, "Test content"));

        assertEquals("Doctor not found", exception.getMessage());
        verify(userRepository).findById(doctorId);
        verify(userRepository, never()).findById(patientId);
    }

    @Test
    void addMedicalHistory_DoctorNotActive_ShouldThrowException() {
        doctor.setStatus("INACTIVE");
        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> medicalHistoryService.addMedicalHistory(doctorId, patientId, "Test content"));

        assertEquals("Invalid doctor or doctor is not active", exception.getMessage());
        verify(userRepository).findById(doctorId);
        verify(userRepository, never()).findById(patientId);
    }

    @Test
    void addMedicalHistory_DoctorNotDoctorRole_ShouldThrowException() {
        doctor.setRole("ADMIN");
        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> medicalHistoryService.addMedicalHistory(doctorId, patientId, "Test content"));

        assertEquals("Invalid doctor or doctor is not active", exception.getMessage());
        verify(userRepository).findById(doctorId);
        verify(userRepository, never()).findById(patientId);
    }

    @Test
    void addMedicalHistory_PatientNotFound_ShouldThrowException() {
        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(userRepository.findById(patientId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> medicalHistoryService.addMedicalHistory(doctorId, patientId, "Test content"));

        assertEquals("Patient not found", exception.getMessage());
        verify(userRepository).findById(doctorId);
        verify(userRepository).findById(patientId);
        verify(turnAssignedRepository, never()).existsByDoctor_IdAndPatient_Id(any(), any());
    }

    @Test
    void addMedicalHistory_PatientNotActive_ShouldThrowException() {
        patient.setStatus("INACTIVE");
        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> medicalHistoryService.addMedicalHistory(doctorId, patientId, "Test content"));

        assertEquals("Invalid patient or patient is not active", exception.getMessage());
        verify(userRepository).findById(doctorId);
        verify(userRepository).findById(patientId);
        verify(turnAssignedRepository, never()).existsByDoctor_IdAndPatient_Id(any(), any());
    }

    @Test
    void addMedicalHistory_PatientNotPatientRole_ShouldThrowException() {
        patient.setRole("DOCTOR");
        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> medicalHistoryService.addMedicalHistory(doctorId, patientId, "Test content"));

        assertEquals("Invalid patient or patient is not active", exception.getMessage());
        verify(userRepository).findById(doctorId);
        verify(userRepository).findById(patientId);
        verify(turnAssignedRepository, never()).existsByDoctor_IdAndPatient_Id(any(), any());
    }

    @Test
    void addMedicalHistory_NoAppointmentsBetweenDoctorAndPatient_ShouldThrowException() {
        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(turnAssignedRepository.existsByDoctor_IdAndPatient_Id(doctorId, patientId)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> medicalHistoryService.addMedicalHistory(doctorId, patientId, "Test content"));

        assertEquals("Doctor can only add medical history for patients they have treated", exception.getMessage());
        verify(userRepository).findById(doctorId);
        verify(userRepository).findById(patientId);
        verify(turnAssignedRepository).existsByDoctor_IdAndPatient_Id(doctorId, patientId);
        verify(medicalHistoryRepository, never()).save(any());
    }
}