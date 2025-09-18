package com.medibook.api.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medibook.api.dto.Availability.DayAvailabilityDTO;
import com.medibook.api.dto.Availability.TimeRangeDTO;
import com.medibook.api.entity.DoctorProfile;
import com.medibook.api.entity.User;
import com.medibook.api.repository.DoctorProfileRepository;
import com.medibook.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class DoctorProfileJsonIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DoctorProfileRepository doctorProfileRepository;

    @Autowired
    private UserRepository userRepository;

    private ObjectMapper objectMapper = new ObjectMapper();
    private User doctorUser;
    private DoctorProfile doctorProfile;

    @BeforeEach
    void setUp() {
        // Create a doctor user
        doctorUser = new User();
        doctorUser.setEmail("doctor@test.com");
        doctorUser.setDni(12345678L);
        doctorUser.setPasswordHash("hashedPassword");
        doctorUser.setName("Dr. John");
        doctorUser.setSurname("Doe");
        doctorUser.setPhone("123-456-7890");
        doctorUser.setBirthdate(LocalDate.of(1980, 1, 1));
        doctorUser.setGender("M");
        doctorUser.setRole("DOCTOR");
        doctorUser.setStatus("APPROVED");
        doctorUser.setEmailVerified(true);
        doctorUser = userRepository.save(doctorUser);

        // Create a doctor profile
        doctorProfile = new DoctorProfile();
        doctorProfile.setUser(doctorUser);
        doctorProfile.setSpecialty("Cardiology");
        doctorProfile.setMedicalLicense("LIC123456");
        doctorProfile.setSlotDurationMin(30);
    }

    @Test
    void testSaveAndRetrieveJsonAvailability() throws Exception {
        List<DayAvailabilityDTO> availabilityData = List.of(
            new DayAvailabilityDTO("MONDAY", true, List.of(
                new TimeRangeDTO("09:00", "12:00"),
                new TimeRangeDTO("14:00", "18:00")
            )),
            new DayAvailabilityDTO("TUESDAY", true, List.of(
                new TimeRangeDTO("08:00", "16:00")
            )),
            new DayAvailabilityDTO("WEDNESDAY", false, Collections.emptyList()),
            new DayAvailabilityDTO("THURSDAY", true, List.of(
                new TimeRangeDTO("10:00", "15:00")
            )),
            new DayAvailabilityDTO("FRIDAY", true, List.of(
                new TimeRangeDTO("09:00", "17:00")
            )),
            new DayAvailabilityDTO("SATURDAY", false, Collections.emptyList()),
            new DayAvailabilityDTO("SUNDAY", false, Collections.emptyList())
        );

        String jsonAvailability = objectMapper.writeValueAsString(availabilityData);
        doctorProfile.setAvailabilitySchedule(jsonAvailability);

        DoctorProfile savedProfile = doctorProfileRepository.save(doctorProfile);
        entityManager.flush();
        entityManager.clear();

        Optional<DoctorProfile> retrievedProfileOpt = doctorProfileRepository.findById(savedProfile.getId());
        assertTrue(retrievedProfileOpt.isPresent());

        DoctorProfile retrievedProfile = retrievedProfileOpt.get();
        assertNotNull(retrievedProfile.getAvailabilitySchedule());

        // Parse JSON back to objects
        List<DayAvailabilityDTO> retrievedAvailability = objectMapper.readValue(
            retrievedProfile.getAvailabilitySchedule(),
            new TypeReference<List<DayAvailabilityDTO>>() {}
        );

        // Verify data integrity
        assertEquals(7, retrievedAvailability.size());

        // Check Monday
        DayAvailabilityDTO monday = findDayByName(retrievedAvailability, "MONDAY");
        assertNotNull(monday);
        assertTrue(monday.getEnabled());
        assertEquals(2, monday.getRanges().size());
        assertEquals("09:00", monday.getRanges().get(0).getStart());
        assertEquals("12:00", monday.getRanges().get(0).getEnd());
        assertEquals("14:00", monday.getRanges().get(1).getStart());
        assertEquals("18:00", monday.getRanges().get(1).getEnd());

        // Check Tuesday
        DayAvailabilityDTO tuesday = findDayByName(retrievedAvailability, "TUESDAY");
        assertNotNull(tuesday);
        assertTrue(tuesday.getEnabled());
        assertEquals(1, tuesday.getRanges().size());
        assertEquals("08:00", tuesday.getRanges().get(0).getStart());
        assertEquals("16:00", tuesday.getRanges().get(0).getEnd());

        // Check Wednesday (disabled)
        DayAvailabilityDTO wednesday = findDayByName(retrievedAvailability, "WEDNESDAY");
        assertNotNull(wednesday);
        assertFalse(wednesday.getEnabled());
        assertTrue(wednesday.getRanges().isEmpty());
    }

    @Test
    void testSaveNullAvailability() {
        // Arrange
        doctorProfile.setAvailabilitySchedule(null);

        // Act
        DoctorProfile savedProfile = doctorProfileRepository.save(doctorProfile);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<DoctorProfile> retrievedProfileOpt = doctorProfileRepository.findById(savedProfile.getId());
        assertTrue(retrievedProfileOpt.isPresent());

        DoctorProfile retrievedProfile = retrievedProfileOpt.get();
        assertNull(retrievedProfile.getAvailabilitySchedule());
    }

    @Test
    void testSaveEmptyJsonArray() throws Exception {
        // Arrange
        List<DayAvailabilityDTO> emptyAvailability = Collections.emptyList();
        String jsonAvailability = objectMapper.writeValueAsString(emptyAvailability);
        doctorProfile.setAvailabilitySchedule(jsonAvailability);

        // Act
        DoctorProfile savedProfile = doctorProfileRepository.save(doctorProfile);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<DoctorProfile> retrievedProfileOpt = doctorProfileRepository.findById(savedProfile.getId());
        assertTrue(retrievedProfileOpt.isPresent());

        DoctorProfile retrievedProfile = retrievedProfileOpt.get();
        assertNotNull(retrievedProfile.getAvailabilitySchedule());
        assertEquals("[]", retrievedProfile.getAvailabilitySchedule());

        List<DayAvailabilityDTO> retrievedAvailability = objectMapper.readValue(
            retrievedProfile.getAvailabilitySchedule(),
            new TypeReference<List<DayAvailabilityDTO>>() {}
        );
        assertTrue(retrievedAvailability.isEmpty());
    }

    @Test
    void testUpdateAvailability() throws Exception {
        // Arrange - Save initial availability
        List<DayAvailabilityDTO> initialAvailability = List.of(
            new DayAvailabilityDTO("MONDAY", true, List.of(
                new TimeRangeDTO("09:00", "17:00")
            ))
        );

        String initialJson = objectMapper.writeValueAsString(initialAvailability);
        doctorProfile.setAvailabilitySchedule(initialJson);
        DoctorProfile savedProfile = doctorProfileRepository.save(doctorProfile);
        entityManager.flush();

        // Act - Update availability
        List<DayAvailabilityDTO> updatedAvailability = List.of(
            new DayAvailabilityDTO("MONDAY", true, List.of(
                new TimeRangeDTO("08:00", "12:00"),
                new TimeRangeDTO("13:00", "18:00")
            )),
            new DayAvailabilityDTO("TUESDAY", true, List.of(
                new TimeRangeDTO("10:00", "16:00")
            ))
        );

        String updatedJson = objectMapper.writeValueAsString(updatedAvailability);
        savedProfile.setAvailabilitySchedule(updatedJson);
        doctorProfileRepository.save(savedProfile);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<DoctorProfile> retrievedProfileOpt = doctorProfileRepository.findById(savedProfile.getId());
        assertTrue(retrievedProfileOpt.isPresent());

        DoctorProfile retrievedProfile = retrievedProfileOpt.get();
        List<DayAvailabilityDTO> retrievedAvailability = objectMapper.readValue(
            retrievedProfile.getAvailabilitySchedule(),
            new TypeReference<List<DayAvailabilityDTO>>() {}
        );

        assertEquals(2, retrievedAvailability.size());

        DayAvailabilityDTO monday = findDayByName(retrievedAvailability, "MONDAY");
        assertNotNull(monday);
        assertEquals(2, monday.getRanges().size());

        DayAvailabilityDTO tuesday = findDayByName(retrievedAvailability, "TUESDAY");
        assertNotNull(tuesday);
        assertEquals(1, tuesday.getRanges().size());
    }

    @Test
    void testComplexJsonStructureWithSpecialCharacters() throws Exception {
        // Arrange - Create availability with edge case time formats
        List<DayAvailabilityDTO> complexAvailability = List.of(
            new DayAvailabilityDTO("MONDAY", true, List.of(
                new TimeRangeDTO("00:00", "23:59")
            )),
            new DayAvailabilityDTO("FRIDAY", true, List.of(
                new TimeRangeDTO("00:30", "01:00"),
                new TimeRangeDTO("23:00", "23:30")
            ))
        );

        String jsonAvailability = objectMapper.writeValueAsString(complexAvailability);
        doctorProfile.setAvailabilitySchedule(jsonAvailability);

        // Act
        DoctorProfile savedProfile = doctorProfileRepository.save(doctorProfile);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<DoctorProfile> retrievedProfileOpt = doctorProfileRepository.findById(savedProfile.getId());
        assertTrue(retrievedProfileOpt.isPresent());

        DoctorProfile retrievedProfile = retrievedProfileOpt.get();
        List<DayAvailabilityDTO> retrievedAvailability = objectMapper.readValue(
            retrievedProfile.getAvailabilitySchedule(),
            new TypeReference<List<DayAvailabilityDTO>>() {}
        );

        DayAvailabilityDTO monday = findDayByName(retrievedAvailability, "MONDAY");
        assertNotNull(monday);
        assertEquals("00:00", monday.getRanges().get(0).getStart());
        assertEquals("23:59", monday.getRanges().get(0).getEnd());

        DayAvailabilityDTO friday = findDayByName(retrievedAvailability, "FRIDAY");
        assertNotNull(friday);
        assertEquals(2, friday.getRanges().size());
        assertEquals("00:30", friday.getRanges().get(0).getStart());
        assertEquals("23:30", friday.getRanges().get(1).getEnd());
    }

    private DayAvailabilityDTO findDayByName(List<DayAvailabilityDTO> availability, String dayName) {
        return availability.stream()
                .filter(day -> dayName.equals(day.getDay()))
                .findFirst()
                .orElse(null);
    }
}