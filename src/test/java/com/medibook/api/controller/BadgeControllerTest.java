package com.medibook.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medibook.api.dto.Badge.BadgeDTO;
import com.medibook.api.dto.Badge.BadgeProgressSummaryDTO;
import com.medibook.api.dto.Badge.DoctorBadgesResponseDTO;
import com.medibook.api.entity.BadgeType;
import com.medibook.api.entity.DoctorBadge;
import com.medibook.api.entity.DoctorBadgeStatistics;
import com.medibook.api.entity.User;
import com.medibook.api.repository.DoctorBadgeRepository;
import com.medibook.api.repository.DoctorBadgeStatisticsRepository;
import com.medibook.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BadgeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorBadgeRepository doctorBadgeRepository;

    @Autowired
    private DoctorBadgeStatisticsRepository doctorBadgeStatisticsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String patientToken;
    private String doctorToken;
    private String adminToken;
    private String otherDoctorToken;
    private User patient;
    private User doctor;
    private User admin;
    private User otherDoctor;

    @BeforeEach
    void setUp() throws Exception {
        patient = createTestPatient();
        doctor = createTestDoctor();
        admin = createTestAdmin();
        otherDoctor = createTestOtherDoctor();

        createDoctorBadgeStatistics(doctor);

        patientToken = getAuthToken(patient.getEmail(), "password123");
        doctorToken = getAuthToken(doctor.getEmail(), "password123");
        adminToken = getAuthToken(admin.getEmail(), "password123");
        otherDoctorToken = getAuthToken(otherDoctor.getEmail(), "password123");
    }

    @Test
    void getDoctorBadges_AsAuthenticatedUser_Success() throws Exception {
        mockMvc.perform(get("/api/badges/doctor/{doctorId}", doctor.getId())
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorId").value(doctor.getId().toString()))
                .andExpect(jsonPath("$.doctorName").value(doctor.getName() + " " + doctor.getSurname()))
                .andExpect(jsonPath("$.totalActiveBadges").isNumber())
                .andExpect(jsonPath("$.qualityOfCareBadges").isArray())
                .andExpect(jsonPath("$.professionalismBadges").isArray())
                .andExpect(jsonPath("$.consistencyBadges").isArray());
    }

    @Test
    void getDoctorBadges_AsDoctor_Success() throws Exception {
        mockMvc.perform(get("/api/badges/doctor/{doctorId}", doctor.getId())
                .header("Authorization", "Bearer " + doctorToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorId").value(doctor.getId().toString()));
    }

    @Test
    void getDoctorBadges_Unauthenticated_Forbidden() throws Exception {
        mockMvc.perform(get("/api/badges/doctor/{doctorId}", doctor.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMyBadges_AsDoctor_Success() throws Exception {
        mockMvc.perform(get("/api/badges/my-badges")
                .header("Authorization", "Bearer " + doctorToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorId").value(doctor.getId().toString()))
                .andExpect(jsonPath("$.doctorName").value(doctor.getName() + " " + doctor.getSurname()));
    }

    @Test
    void getMyBadges_AsPatient_Forbidden() throws Exception {
        mockMvc.perform(get("/api/badges/my-badges")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void getDoctorBadgeProgress_AsAuthenticatedUser_Success() throws Exception {
        mockMvc.perform(get("/api/badges/doctor/{doctorId}/progress", doctor.getId())
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getDoctorBadgeProgress_Unauthenticated_Forbidden() throws Exception {
        mockMvc.perform(get("/api/badges/doctor/{doctorId}/progress", doctor.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMyBadgeProgress_AsDoctor_Success() throws Exception {
        mockMvc.perform(get("/api/badges/my-progress")
                .header("Authorization", "Bearer " + doctorToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getMyBadgeProgress_AsPatient_Forbidden() throws Exception {
        mockMvc.perform(get("/api/badges/my-progress")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void evaluateDoctorBadges_AsAdmin_Success() throws Exception {
        mockMvc.perform(post("/api/badges/doctor/{doctorId}/evaluate", doctor.getId())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void evaluateDoctorBadges_AsSameDoctor_Success() throws Exception {
        mockMvc.perform(post("/api/badges/doctor/{doctorId}/evaluate", doctor.getId())
                .header("Authorization", "Bearer " + doctorToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void evaluateDoctorBadges_AsOtherDoctor_Forbidden() throws Exception {
        mockMvc.perform(post("/api/badges/doctor/{doctorId}/evaluate", doctor.getId())
                .header("Authorization", "Bearer " + otherDoctorToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void evaluateDoctorBadges_AsPatient_Forbidden() throws Exception {
        mockMvc.perform(post("/api/badges/doctor/{doctorId}/evaluate", doctor.getId())
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void evaluateMyBadges_AsDoctor_Success() throws Exception {
        mockMvc.perform(post("/api/badges/evaluate")
                .header("Authorization", "Bearer " + doctorToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void evaluateMyBadges_AsPatient_Forbidden() throws Exception {
        mockMvc.perform(post("/api/badges/evaluate")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    private User createTestPatient() {
        User patient = new User();
        patient.setEmail("patient@example.com");
        patient.setDni(12345678L);
        patient.setPasswordHash(passwordEncoder.encode("password123"));
        patient.setName("John");
        patient.setSurname("Doe");
        patient.setPhone("1234567890");
        patient.setBirthdate(LocalDate.of(1990, 1, 1));
        patient.setGender("MALE");
        patient.setRole("PATIENT");
        patient.setStatus("ACTIVE");
        patient.setEmailVerified(true);
        return userRepository.save(patient);
    }

    private User createTestDoctor() {
        User doctor = new User();
        doctor.setEmail("doctor@example.com");
        doctor.setDni(87654321L);
        doctor.setPasswordHash(passwordEncoder.encode("password123"));
        doctor.setName("Jane");
        doctor.setSurname("Smith");
        doctor.setPhone("0987654321");
        doctor.setBirthdate(LocalDate.of(1985, 5, 15));
        doctor.setGender("FEMALE");
        doctor.setRole("DOCTOR");
        doctor.setStatus("ACTIVE");
        doctor.setEmailVerified(true);
        return userRepository.save(doctor);
    }

    private User createTestAdmin() {
        User admin = new User();
        admin.setEmail("admin@example.com");
        admin.setDni(11223344L);
        admin.setPasswordHash(passwordEncoder.encode("password123"));
        admin.setName("Admin");
        admin.setSurname("User");
        admin.setPhone("1111111111");
        admin.setBirthdate(LocalDate.of(1980, 3, 10));
        admin.setGender("MALE");
        admin.setRole("ADMIN");
        admin.setStatus("ACTIVE");
        admin.setEmailVerified(true);
        return userRepository.save(admin);
    }

    private User createTestOtherDoctor() {
        User otherDoctor = new User();
        otherDoctor.setEmail("otherdoctor@example.com");
        otherDoctor.setDni(55667788L);
        otherDoctor.setPasswordHash(passwordEncoder.encode("password123"));
        otherDoctor.setName("Bob");
        otherDoctor.setSurname("Johnson");
        otherDoctor.setPhone("2222222222");
        otherDoctor.setBirthdate(LocalDate.of(1982, 7, 20));
        otherDoctor.setGender("MALE");
        otherDoctor.setRole("DOCTOR");
        otherDoctor.setStatus("ACTIVE");
        otherDoctor.setEmailVerified(true);
        return userRepository.save(otherDoctor);
    }

    private void createDoctorBadgeStatistics(User doctor) {
        DoctorBadgeStatistics stats = new DoctorBadgeStatistics();
        stats.setDoctor(doctor);
        stats.setTotalRatingsReceived(10);
        stats.setLast100AvgRating(4.5);
        stats.setLast100LowRatingCount(1);
        stats.setTotalTurnsCompleted(25);
        stats.setTotalTurnsCancelled(2);
        stats.setTotalUniquePatients(20);
        stats.setTurnsLast90Days(15);
        stats.setCancellationsLast90Days(1);
        stats.setNoShowsLast90Days(0);
        stats.setLast10RequestsHandled(5);
        stats.setLast30DocumentedCount(8);
        stats.setLast30TotalWords(2400);
        stats.setLast30AvgWordsPerEntry(300.0);
        stats.setLast50CommunicationCount(12);
        stats.setLast50EmpathyCount(15);
        stats.setLast50PunctualityCount(18);
        stats.setReturningPatientsCount(8);
        stats.setSpecialtyRankPercentile(85.0);
        stats.setLastUpdatedAt(OffsetDateTime.now());
        doctorBadgeStatisticsRepository.save(stats);
    }

    private String getAuthToken(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var jsonNode = objectMapper.readTree(response);
        if (jsonNode.get("accessToken") == null) {
            throw new RuntimeException("AccessToken not found in response: " + response);
        }

        return jsonNode.get("accessToken").asText();
    }
}