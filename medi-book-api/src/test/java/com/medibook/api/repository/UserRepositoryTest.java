package com.medibook.api.repository;

import com.medibook.api.config.TestConfig;
import com.medibook.api.entity.User;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class UserRepositoryTest {
    
    @Autowired
    private UserRepository userRepository;

    @Test
    void whenExistsByEmail_thenReturnsTrue() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("hash");
        user.setName("Test");
        user.setSurname("User");
        userRepository.save(user);

        boolean exists = userRepository.existsByEmail("test@example.com");

        assertTrue(exists);
    }

    @Test
    void whenEmailDoesNotExist_thenReturnsFalse() {
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        assertFalse(exists);
    }
}
