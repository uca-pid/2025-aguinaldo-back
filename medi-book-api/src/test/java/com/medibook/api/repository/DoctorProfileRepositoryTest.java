package com.medibook.api.repository;

import com.medibook.api.entity.User;
import com.medibook.api.config.TestConfig;
import com.medibook.api.entity.DoctorProfile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class DoctorProfileRepositoryTest {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DoctorProfileRepository doctorProfileRepository;

    @Test
    void whenSave_thenFindById() {
        User user = new User();
        user.setEmail("doctor@example.com");
        user.setPasswordHash("hash");
        user.setName("Doctor");
        user.setSurname("Test");
        user.setRole("DOCTOR");
        
        DoctorProfile profile = new DoctorProfile();
        profile.setMedicalLicense("ML123");
        profile.setSpecialty("Cardiology");
        profile.setSlotDurationMin(30);
        
        user.setDoctorProfile(profile);
        
        userRepository.save(user);

        var found = doctorProfileRepository.findById(user.getId());
        assertTrue(found.isPresent());
        assertEquals("ML123", found.get().getMedicalLicense());
        assertEquals("Cardiology", found.get().getSpecialty());
        assertEquals(30, found.get().getSlotDurationMin());
        assertEquals(user.getId(), found.get().getId());
    }
}
