package com.medibook.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medibook.api.dto.Availability.*;
import com.medibook.api.service.DoctorAvailabilityService;
import com.medibook.api.service.DoctorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DoctorController.class)
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DoctorService doctorService;

    @MockBean
    private DoctorAvailabilityService availabilityService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID doctorId;
    private DoctorAvailabilityRequestDTO requestDTO;
    private DoctorAvailabilityResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();
        
        // Create test request DTO
        List<DayAvailabilityDTO> weeklyAvailability = List.of(
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
        
        requestDTO = new DoctorAvailabilityRequestDTO(30, weeklyAvailability);
        responseDTO = new DoctorAvailabilityResponseDTO(30, weeklyAvailability);
    }

    @Test
    @WithMockUser(roles = "DOCTOR", username = "doctor@test.com")
    void saveAvailability_Success() throws Exception {
        // Arrange
        doNothing().when(availabilityService).saveAvailability(eq(doctorId), any(DoctorAvailabilityRequestDTO.class));

        // Act & Assert
        mockMvc.perform(post("/api/doctors/{doctorId}/availability", doctorId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk());

        verify(availabilityService).saveAvailability(eq(doctorId), any(DoctorAvailabilityRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "DOCTOR", username = "doctor@test.com")
    void saveAvailability_InvalidData_BadRequest() throws Exception {
        // Arrange - Create invalid request with null slot duration
        DoctorAvailabilityRequestDTO invalidRequest = new DoctorAvailabilityRequestDTO(null, requestDTO.getWeeklyAvailability());

        // Act & Assert
        mockMvc.perform(post("/api/doctors/{doctorId}/availability", doctorId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(availabilityService, never()).saveAvailability(any(), any());
    }

    @Test
    @WithMockUser(roles = "DOCTOR", username = "doctor@test.com")
    void saveAvailability_SlotDurationTooSmall_BadRequest() throws Exception {
        // Arrange - Create invalid request with slot duration too small
        DoctorAvailabilityRequestDTO invalidRequest = new DoctorAvailabilityRequestDTO(3, requestDTO.getWeeklyAvailability());

        // Act & Assert
        mockMvc.perform(post("/api/doctors/{doctorId}/availability", doctorId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(availabilityService, never()).saveAvailability(any(), any());
    }

    @Test
    @WithMockUser(roles = "DOCTOR", username = "doctor@test.com")
    void saveAvailability_NullWeeklyAvailability_BadRequest() throws Exception {
        // Arrange - Create invalid request with null weekly availability
        DoctorAvailabilityRequestDTO invalidRequest = new DoctorAvailabilityRequestDTO(30, null);

        // Act & Assert
        mockMvc.perform(post("/api/doctors/{doctorId}/availability", doctorId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(availabilityService, never()).saveAvailability(any(), any());
    }

    @Test
    void saveAvailability_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/doctors/{doctorId}/availability", doctorId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isUnauthorized());

        verify(availabilityService, never()).saveAvailability(any(), any());
    }

    @Test
    @WithMockUser(roles = "PATIENT", username = "patient@test.com")
    void saveAvailability_Forbidden_WrongRole() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/doctors/{doctorId}/availability", doctorId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isForbidden());

        verify(availabilityService, never()).saveAvailability(any(), any());
    }

    @Test
    @WithMockUser(roles = "DOCTOR", username = "doctor@test.com")
    void saveAvailability_ServiceException() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Database error")).when(availabilityService)
                .saveAvailability(eq(doctorId), any(DoctorAvailabilityRequestDTO.class));

        // Act & Assert
        mockMvc.perform(post("/api/doctors/{doctorId}/availability", doctorId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isInternalServerError());

        verify(availabilityService).saveAvailability(eq(doctorId), any(DoctorAvailabilityRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "DOCTOR", username = "doctor@test.com")
    void getAvailability_Success() throws Exception {
        // Arrange
        when(availabilityService.getAvailability(doctorId)).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(get("/api/doctors/{doctorId}/availability", doctorId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.slotDurationMin").value(30))
                .andExpect(jsonPath("$.weeklyAvailability").isArray())
                .andExpect(jsonPath("$.weeklyAvailability.length()").value(7));

        verify(availabilityService).getAvailability(doctorId);
    }

    @Test
    void getAvailability_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/doctors/{doctorId}/availability", doctorId))
                .andExpect(status().isUnauthorized());

        verify(availabilityService, never()).getAvailability(any());
    }

    @Test
    @WithMockUser(roles = "PATIENT", username = "patient@test.com")
    void getAvailability_Forbidden_WrongRole() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/doctors/{doctorId}/availability", doctorId))
                .andExpect(status().isForbidden());

        verify(availabilityService, never()).getAvailability(any());
    }

    @Test
    @WithMockUser(roles = "DOCTOR", username = "doctor@test.com")
    void getAvailability_DoctorNotFound() throws Exception {
        // Arrange
        when(availabilityService.getAvailability(doctorId))
                .thenThrow(new RuntimeException("Doctor not found with id: " + doctorId));

        // Act & Assert
        mockMvc.perform(get("/api/doctors/{doctorId}/availability", doctorId))
                .andExpect(status().isInternalServerError());

        verify(availabilityService).getAvailability(doctorId);
    }

    @Test
    @WithMockUser(roles = "PATIENT", username = "patient@test.com")
    void getAvailableSlots_Success() throws Exception {
        // Arrange
        LocalDate fromDate = LocalDate.now();
        LocalDate toDate = fromDate.plusDays(7);
        
        List<AvailableSlotDTO> expectedSlots = List.of(
            new AvailableSlotDTO(fromDate, LocalTime.of(9, 0), LocalTime.of(9, 30), "MONDAY"),
            new AvailableSlotDTO(fromDate, LocalTime.of(9, 30), LocalTime.of(10, 0), "MONDAY"),
            new AvailableSlotDTO(fromDate, LocalTime.of(10, 0), LocalTime.of(10, 30), "MONDAY")
        );
        
        when(availabilityService.getAvailableSlots(doctorId, fromDate, toDate))
                .thenReturn(expectedSlots);

        // Act & Assert
        mockMvc.perform(get("/api/doctors/{doctorId}/available-slots", doctorId)
                .param("fromDate", fromDate.toString())
                .param("toDate", toDate.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].startTime").exists())
                .andExpect(jsonPath("$[0].endTime").exists());

        verify(availabilityService).getAvailableSlots(doctorId, fromDate, toDate);
    }

    @Test
    @WithMockUser(roles = "PATIENT", username = "patient@test.com")
    void getAvailableSlots_MissingParameters_BadRequest() throws Exception {
        // Act & Assert - Missing fromDate parameter
        mockMvc.perform(get("/api/doctors/{doctorId}/available-slots", doctorId)
                .param("toDate", LocalDate.now().toString()))
                .andExpect(status().isBadRequest());

        // Act & Assert - Missing toDate parameter
        mockMvc.perform(get("/api/doctors/{doctorId}/available-slots", doctorId)
                .param("fromDate", LocalDate.now().toString()))
                .andExpect(status().isBadRequest());

        verify(availabilityService, never()).getAvailableSlots(any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "PATIENT", username = "patient@test.com")
    void getAvailableSlots_InvalidDateFormat_BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/doctors/{doctorId}/available-slots", doctorId)
                .param("fromDate", "invalid-date")
                .param("toDate", "2025-12-31"))
                .andExpect(status().isBadRequest());

        verify(availabilityService, never()).getAvailableSlots(any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "PATIENT", username = "patient@test.com")
    void getAvailableSlots_ServiceException() throws Exception {
        // Arrange
        LocalDate fromDate = LocalDate.now();
        LocalDate toDate = fromDate.plusDays(7);
        
        when(availabilityService.getAvailableSlots(doctorId, fromDate, toDate))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(get("/api/doctors/{doctorId}/available-slots", doctorId)
                .param("fromDate", fromDate.toString())
                .param("toDate", toDate.toString()))
                .andExpect(status().isInternalServerError());

        verify(availabilityService).getAvailableSlots(doctorId, fromDate, toDate);
    }
}