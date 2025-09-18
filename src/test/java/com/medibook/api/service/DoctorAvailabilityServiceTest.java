package com.medibook.api.service;

import com.medibook.api.dto.Availability.DayAvailabilityDTO;
import com.medibook.api.dto.Availability.DoctorAvailabilityRequestDTO;
import com.medibook.api.dto.Availability.DoctorAvailabilityResponseDTO;
import com.medibook.api.dto.Availability.TimeRangeDTO;
import com.medibook.api.entity.DoctorProfile;
import com.medibook.api.entity.User;
import com.medibook.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorAvailabilityServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DoctorAvailabilityService doctorAvailabilityService;

    private User doctorUser;
    private DoctorProfile doctorProfile;
    private DoctorAvailabilityRequestDTO testRequest;

    @BeforeEach
    void setUp() {
        doctorUser = new User();
        doctorUser.setId(UUID.randomUUID());
        doctorUser.setEmail("doctor@test.com");

        doctorProfile = new DoctorProfile();
        doctorProfile.setId(doctorUser.getId());
        doctorProfile.setSpecialty("Cardiology");
        doctorProfile.setSlotDurationMin(30);
        
        doctorUser.setDoctorProfile(doctorProfile);

        // Create test request data
        List<DayAvailabilityDTO> weeklyAvailability = List.of(
            new DayAvailabilityDTO("MONDAY", true, List.of(
                new TimeRangeDTO("09:00", "12:00")
            ))
        );
        
        testRequest = new DoctorAvailabilityRequestDTO(30, weeklyAvailability);
    }

    @Test
    void saveAvailability_DoctorNotFound() {
        UUID doctorId = UUID.randomUUID();
        
        when(userRepository.findById(doctorId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> doctorAvailabilityService.saveAvailability(doctorId, testRequest));
        
        assertEquals("Doctor not found", exception.getMessage());
        verify(userRepository).findById(doctorId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void saveAvailability_NoDoctorProfile() {
        UUID doctorId = doctorUser.getId();
        doctorUser.setDoctorProfile(null); // No doctor profile
        
        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctorUser));

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> doctorAvailabilityService.saveAvailability(doctorId, testRequest));
        
        assertEquals("Doctor profile not found", exception.getMessage());
        verify(userRepository).findById(doctorId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void getAvailability_DoctorNotFound() {
        UUID doctorId = UUID.randomUUID();
        
        when(userRepository.findById(doctorId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> doctorAvailabilityService.getAvailability(doctorId));
        
        assertEquals("Doctor not found", exception.getMessage());
        verify(userRepository).findById(doctorId);
    }

    @Test
    void getAvailability_NoScheduleReturnsEmpty() {
        UUID doctorId = doctorUser.getId();
        doctorProfile.setAvailabilitySchedule(null);
        
        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctorUser));

        DoctorAvailabilityResponseDTO result = doctorAvailabilityService.getAvailability(doctorId);

        assertNotNull(result);
        assertEquals(30, result.getSlotDurationMin());
        assertNotNull(result.getWeeklyAvailability());
        assertTrue(result.getWeeklyAvailability().isEmpty());
        
        verify(userRepository).findById(doctorId);
    }

    @Test 
    void getAvailability_NoDoctorProfile() {
        UUID doctorId = doctorUser.getId();
        doctorUser.setDoctorProfile(null);
        
        when(userRepository.findById(doctorId)).thenReturn(Optional.of(doctorUser));

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> doctorAvailabilityService.getAvailability(doctorId));
        
        assertEquals("Doctor profile not found", exception.getMessage());
        verify(userRepository).findById(doctorId);
    }
}