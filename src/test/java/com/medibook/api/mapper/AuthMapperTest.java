package com.medibook.api.mapper;

import com.medibook.api.dto.SignInResponseDTO;
import com.medibook.api.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AuthMapperTest {

    @InjectMocks
    private AuthMapper authMapper;

    @Test
    void toSignInResponse_WithTokens_ShouldMapCorrectly() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setName("John");
        user.setSurname("Doe");
        user.setRole("PATIENT");
        user.setStatus("ACTIVE");
        
        String accessToken = "access-token-123";
        String refreshToken = "refresh-token-456";

        SignInResponseDTO result = authMapper.toSignInResponse(user, accessToken, refreshToken);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.email()).isEqualTo("test@example.com");
        assertThat(result.name()).isEqualTo("John");
        assertThat(result.surname()).isEqualTo("Doe");
        assertThat(result.role()).isEqualTo("PATIENT");
        assertThat(result.accessToken()).isEqualTo(accessToken);
        assertThat(result.refreshToken()).isEqualTo(refreshToken);
    }

    @Test
    void toSignInResponse_WithoutTokens_ShouldMapCorrectlyWithNullTokens() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setName("Jane");
        user.setSurname("Smith");
        user.setRole("DOCTOR");
        user.setStatus("ACTIVE");

        SignInResponseDTO result = authMapper.toSignInResponse(user);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.email()).isEqualTo("test@example.com");
        assertThat(result.name()).isEqualTo("Jane");
        assertThat(result.surname()).isEqualTo("Smith");
        assertThat(result.role()).isEqualTo("DOCTOR");
        assertThat(result.accessToken()).isNull();
        assertThat(result.refreshToken()).isNull();
    }
}