package com.medibook.api.mapper;

import com.medibook.api.dto.Turn.TurnCreateRequestDTO;
import com.medibook.api.dto.Turn.TurnResponseDTO;
import com.medibook.api.entity.DoctorProfile;
import com.medibook.api.entity.TurnAssigned;
import com.medibook.api.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TurnAssignedMapperTest {

    private TurnAssignedMapper turnAssignedMapper;
    private User doctorUser;
    private User patientUser;
    private DoctorProfile doctorProfile;
    private TurnCreateRequestDTO turnCreateRequestDTO;
    private TurnAssigned turnAssigned;
    private OffsetDateTime scheduledDateTime;
    private UUID doctorId;
    private UUID patientId;
    private UUID turnId;

    @BeforeEach
    void setUp() {
        turnAssignedMapper = new TurnAssignedMapper();
        
        doctorId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        turnId = UUID.randomUUID();
        scheduledDateTime = OffsetDateTime.now().plusDays(1);

        doctorUser = createUser(doctorId, "doctor@test.com", 12345678L, "DOCTOR", "ACTIVE", "Dr. Juan", "Pérez");
        patientUser = createUser(patientId, "patient@test.com", 87654321L, "PATIENT", "ACTIVE", "María", "González");

        doctorProfile = new DoctorProfile();
        doctorProfile.setId(doctorId);
        doctorProfile.setUser(doctorUser);
        doctorProfile.setSpecialty("Cardiología");
        doctorProfile.setMedicalLicense("MED123456");
        doctorProfile.setSlotDurationMin(30);
        
        doctorUser.setDoctorProfile(doctorProfile);

        turnCreateRequestDTO = new TurnCreateRequestDTO();
        turnCreateRequestDTO.setScheduledAt(scheduledDateTime);

        turnAssigned = TurnAssigned.builder()
                .id(turnId)
                .doctor(doctorUser)
                .patient(patientUser)
                .scheduledAt(scheduledDateTime)
                .status("RESERVED")
                .build();
    }

    private User createUser(UUID id, String email, Long dni, String role, String status, String name, String surname) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setDni(dni);
        user.setPasswordHash("hashedPassword");
        user.setName(name);
        user.setSurname(surname);
        user.setPhone("1234567890");
        user.setBirthdate(LocalDate.of(1990, 1, 1));
        user.setGender("MALE");
        user.setEmailVerified(true);
        user.setStatus(status);
        user.setRole(role);
        return user;
    }

    @Test
    void toEntity_ValidDTOAndDoctor_ReturnsCorrectEntity() {
        TurnAssigned result = turnAssignedMapper.toEntity(turnCreateRequestDTO, doctorUser);

        assertNotNull(result);
        assertEquals(doctorUser, result.getDoctor());
        assertEquals(scheduledDateTime, result.getScheduledAt());
        assertEquals("AVAILABLE", result.getStatus());
        assertNull(result.getId());
        assertNull(result.getPatient());
    }

    @Test
    void toEntity_NullDTO_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> 
                turnAssignedMapper.toEntity(null, doctorUser));
    }

    @Test
    void toEntity_NullDoctor_ReturnsEntityWithNullDoctor() {
        TurnAssigned result = turnAssignedMapper.toEntity(turnCreateRequestDTO, null);

        assertNotNull(result);
        assertNull(result.getDoctor());
        assertEquals(scheduledDateTime, result.getScheduledAt());
        assertEquals("AVAILABLE", result.getStatus());
    }

    @Test
    void toEntity_NullScheduledAt_ReturnsEntityWithNullScheduledAt() {
        turnCreateRequestDTO.setScheduledAt(null);

        TurnAssigned result = turnAssignedMapper.toEntity(turnCreateRequestDTO, doctorUser);

        assertNotNull(result);
        assertEquals(doctorUser, result.getDoctor());
        assertNull(result.getScheduledAt());
        assertEquals("AVAILABLE", result.getStatus());
    }

    @Test
    void toDTO_CompleteEntityWithPatient_ReturnsCompleteDTO() {
        TurnResponseDTO result = turnAssignedMapper.toDTO(turnAssigned);

        assertNotNull(result);
        assertEquals(turnId, result.getId());
        assertEquals(doctorId, result.getDoctorId());
        assertEquals("Dr. Juan Pérez", result.getDoctorName());
        assertEquals("Cardiología", result.getDoctorSpecialty());
        assertEquals(patientId, result.getPatientId());
        assertEquals("María González", result.getPatientName());
        assertEquals(scheduledDateTime, result.getScheduledAt());
        assertEquals("RESERVED", result.getStatus());
    }

    @Test
    void toDTO_EntityWithoutPatient_ReturnsDTOWithNullPatientFields() {
        turnAssigned = TurnAssigned.builder()
                .id(turnId)
                .doctor(doctorUser)
                .patient(null)
                .scheduledAt(scheduledDateTime)
                .status("AVAILABLE")
                .build();

        TurnResponseDTO result = turnAssignedMapper.toDTO(turnAssigned);

        assertNotNull(result);
        assertEquals(turnId, result.getId());
        assertEquals(doctorId, result.getDoctorId());
        assertEquals("Dr. Juan Pérez", result.getDoctorName());
        assertEquals("Cardiología", result.getDoctorSpecialty());
        assertNull(result.getPatientId());
        assertNull(result.getPatientName());
        assertEquals(scheduledDateTime, result.getScheduledAt());
        assertEquals("AVAILABLE", result.getStatus());
    }

    @Test
    void toDTO_EntityWithoutDoctorProfile_ReturnsDTOWithNullSpecialty() {
        doctorUser.setDoctorProfile(null);
        turnAssigned = TurnAssigned.builder()
                .id(turnId)
                .doctor(doctorUser)
                .patient(patientUser)
                .scheduledAt(scheduledDateTime)
                .status("RESERVED")
                .build();

        TurnResponseDTO result = turnAssignedMapper.toDTO(turnAssigned);

        assertNotNull(result);
        assertEquals(turnId, result.getId());
        assertEquals(doctorId, result.getDoctorId());
        assertEquals("Dr. Juan Pérez", result.getDoctorName());
        assertNull(result.getDoctorSpecialty());
        assertEquals(patientId, result.getPatientId());
        assertEquals("María González", result.getPatientName());
        assertEquals(scheduledDateTime, result.getScheduledAt());
        assertEquals("RESERVED", result.getStatus());
    }

    @Test
    void toDTO_NullEntity_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> 
                turnAssignedMapper.toDTO(null));
    }

    @Test
    void toDTO_EntityWithNullDoctor_ThrowsNullPointerException() {
        turnAssigned = TurnAssigned.builder()
                .id(turnId)
                .doctor(null)
                .patient(patientUser)
                .scheduledAt(scheduledDateTime)
                .status("RESERVED")
                .build();

        assertThrows(NullPointerException.class, () -> 
                turnAssignedMapper.toDTO(turnAssigned));
    }

    @Test
    void toDTO_DoctorWithNullName_HandlesConcatenationGracefully() {
        doctorUser.setName(null);
        doctorUser.setSurname("Pérez");

        TurnResponseDTO result = turnAssignedMapper.toDTO(turnAssigned);

        assertNotNull(result);
        assertEquals("null Pérez", result.getDoctorName());
    }

    @Test
    void toDTO_DoctorWithNullSurname_HandlesConcatenationGracefully() {
        doctorUser.setName("Dr. Juan");
        doctorUser.setSurname(null);

        TurnResponseDTO result = turnAssignedMapper.toDTO(turnAssigned);

        assertNotNull(result);
        assertEquals("Dr. Juan null", result.getDoctorName());
    }

    @Test
    void toDTO_DoctorWithBothNamesNull_HandlesConcatenationGracefully() {
        doctorUser.setName(null);
        doctorUser.setSurname(null);

        TurnResponseDTO result = turnAssignedMapper.toDTO(turnAssigned);

        assertNotNull(result);
        assertEquals("null null", result.getDoctorName());
    }

    @Test
    void toDTO_PatientWithNullName_HandlesConcatenationGracefully() {
        patientUser.setName(null);
        patientUser.setSurname("González");

        TurnResponseDTO result = turnAssignedMapper.toDTO(turnAssigned);

        assertNotNull(result);
        assertEquals("null González", result.getPatientName());
    }

    @Test
    void toDTO_PatientWithNullSurname_HandlesConcatenationGracefully() {
        patientUser.setName("María");
        patientUser.setSurname(null);

        TurnResponseDTO result = turnAssignedMapper.toDTO(turnAssigned);

        assertNotNull(result);
        assertEquals("María null", result.getPatientName());
    }

    @Test
    void toDTO_PatientWithBothNamesNull_HandlesConcatenationGracefully() {
        patientUser.setName(null);
        patientUser.setSurname(null);

        TurnResponseDTO result = turnAssignedMapper.toDTO(turnAssigned);

        assertNotNull(result);
        assertEquals("null null", result.getPatientName());
    }

    @Test
    void toDTO_DoctorWithSpecialCharactersInName_HandlesCorrectly() {
        doctorUser.setName("José María");
        doctorUser.setSurname("García-López");

        TurnResponseDTO result = turnAssignedMapper.toDTO(turnAssigned);

        assertNotNull(result);
        assertEquals("José María García-López", result.getDoctorName());
    }

    @Test
    void toDTO_PatientWithSpecialCharactersInName_HandlesCorrectly() {
        patientUser.setName("Ana Sofía");
        patientUser.setSurname("Rodríguez-Martínez");

        TurnResponseDTO result = turnAssignedMapper.toDTO(turnAssigned);

        assertNotNull(result);
        assertEquals("Ana Sofía Rodríguez-Martínez", result.getPatientName());
    }

    @Test
    void toDTO_SpecialtyWithSpecialCharacters_HandlesCorrectly() {
        doctorProfile.setSpecialty("Ginecología y Obstetricia");

        TurnResponseDTO result = turnAssignedMapper.toDTO(turnAssigned);

        assertNotNull(result);
        assertEquals("Ginecología y Obstetricia", result.getDoctorSpecialty());
    }

    @Test
    void toEntity_DefaultStatusIsAvailable() {
        TurnAssigned result = turnAssignedMapper.toEntity(turnCreateRequestDTO, doctorUser);

        assertNotNull(result);
        assertEquals("AVAILABLE", result.getStatus());
    }

    @Test
    void toDTO_AllStatusesHandledCorrectly() {
        String[] statuses = {"AVAILABLE", "RESERVED", "COMPLETED", "CANCELLED"};
        
        for (String status : statuses) {
            turnAssigned.setStatus(status);
            TurnResponseDTO result = turnAssignedMapper.toDTO(turnAssigned);
            
            assertNotNull(result);
            assertEquals(status, result.getStatus());
        }
    }

    @Test
    void toDTO_NullStatus_ReturnsNullStatus() {
        turnAssigned.setStatus(null);

        TurnResponseDTO result = turnAssignedMapper.toDTO(turnAssigned);

        assertNotNull(result);
        assertNull(result.getStatus());
    }

    @Test
    void toEntity_FutureDateTime_HandlesCorrectly() {
        OffsetDateTime futureDateTime = OffsetDateTime.now().plusDays(30);
        turnCreateRequestDTO.setScheduledAt(futureDateTime);

        TurnAssigned result = turnAssignedMapper.toEntity(turnCreateRequestDTO, doctorUser);

        assertNotNull(result);
        assertEquals(futureDateTime, result.getScheduledAt());
    }

    @Test
    void toDTO_PastDateTime_HandlesCorrectly() {
        OffsetDateTime pastDateTime = OffsetDateTime.now().minusDays(30);
        turnAssigned.setScheduledAt(pastDateTime);

        TurnResponseDTO result = turnAssignedMapper.toDTO(turnAssigned);

        assertNotNull(result);
        assertEquals(pastDateTime, result.getScheduledAt());
    }

    @Test
    void toEntityThenToDTO_PreservesScheduledAt() {
        TurnAssigned entity = turnAssignedMapper.toEntity(turnCreateRequestDTO, doctorUser);
        entity.setId(turnId);
        entity.setPatient(patientUser);
        entity.setStatus("RESERVED");
        
        TurnResponseDTO dto = turnAssignedMapper.toDTO(entity);

        assertEquals(turnCreateRequestDTO.getScheduledAt(), dto.getScheduledAt());
        assertEquals(doctorId, dto.getDoctorId());
        assertEquals(patientId, dto.getPatientId());
        assertEquals("RESERVED", dto.getStatus());
    }

    @Test
    void toDTO_LargeStringNames_HandlesEfficiently() {
        String longName = "A".repeat(1000);
        String longSurname = "B".repeat(1000);
        doctorUser.setName(longName);
        doctorUser.setSurname(longSurname);

        long startTime = System.currentTimeMillis();
        TurnResponseDTO result = turnAssignedMapper.toDTO(turnAssigned);
        long endTime = System.currentTimeMillis();

        assertNotNull(result);
        assertEquals(longName + " " + longSurname, result.getDoctorName());
        assertTrue(endTime - startTime < 100, "Should handle large strings efficiently");
    }

    @Test
    void toDTO_MultipleCallsPerformance_HandlesEfficiently() {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            turnAssignedMapper.toDTO(turnAssigned);
        }
        long endTime = System.currentTimeMillis();

        assertTrue(endTime - startTime < 1000, "Should handle multiple mappings efficiently");
    }

    @Test
    void toDTO_DoesNotModifyOriginalEntity() {
        TurnAssigned originalEntity = TurnAssigned.builder()
                .id(turnId)
                .doctor(doctorUser)
                .patient(patientUser)
                .scheduledAt(scheduledDateTime)
                .status("RESERVED")
                .build();

        TurnResponseDTO result = turnAssignedMapper.toDTO(originalEntity);
        
        result.setStatus("MODIFIED");

        assertEquals("RESERVED", originalEntity.getStatus()); 
        assertNotEquals(originalEntity.getStatus(), result.getStatus());
    }

    @Test
    void toEntity_DoesNotModifyOriginalDTO() {
        TurnCreateRequestDTO originalDTO = new TurnCreateRequestDTO();
        originalDTO.setScheduledAt(scheduledDateTime);

        TurnAssigned result = turnAssignedMapper.toEntity(originalDTO, doctorUser);
        
        result.setStatus("MODIFIED");

        assertEquals(scheduledDateTime, originalDTO.getScheduledAt()); 
    }
}