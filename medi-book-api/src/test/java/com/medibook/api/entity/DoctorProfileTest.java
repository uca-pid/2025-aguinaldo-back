package com.medibook.api.entity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DoctorProfileTest {
    
    @Test
    void whenCreated_thenAllFieldsAreSet() {
        User user = new User();
        user.setId(UUID.randomUUID());
        String medicalLicense = "ML123";
        String specialty = "Cardiology";
        int slotDurationMin = 30;

        DoctorProfile profile = new DoctorProfile();
        profile.setUser(user);
        profile.setMedicalLicense(medicalLicense);
        profile.setSpecialty(specialty);
        profile.setSlotDurationMin(slotDurationMin);

        assertEquals(user, profile.getUser());
        assertEquals(user.getId(), profile.getUser().getId());
        assertEquals(medicalLicense, profile.getMedicalLicense());
        assertEquals(specialty, profile.getSpecialty());
        assertEquals(slotDurationMin, profile.getSlotDurationMin());
    }

    @Test
    void whenDefaultSlotDuration_thenReturns15Minutes() {
        DoctorProfile profile = new DoctorProfile();

        assertEquals(15, profile.getSlotDurationMin());
    }
}