package com.medibook.api.repository;

import com.medibook.api.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TestEntityManager entityManager;

    @Test
    void whenExistsByEmail_thenReturnsTrue() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("hash");
        user.setName("Test");
        user.setSurname("User");
        
        entityManager.persistAndFlush(user);
        
        boolean exists = userRepository.existsByEmail("test@example.com");
        
        assertTrue(exists);
    }

    @Test
    void whenEmailDoesNotExist_thenReturnsFalse() {
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");
        
        assertFalse(exists);
    }
}