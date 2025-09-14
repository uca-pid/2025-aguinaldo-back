package com.medibook.api.dto;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class RegisterResponseDTOTest {

    @Test
    void whenCreated_thenAllFieldsAreSet() {
        UUID id = UUID.randomUUID();
        String email = "test@example.com";
        String name = "John";
        String surname = "Doe";
        String role = "PATIENT";

        RegisterResponseDTO response = new RegisterResponseDTO(id, email, name, surname, role, "ACTIVE");

        assertEquals(id, response.id());
        assertEquals(email, response.email());
        assertEquals(name, response.name());
        assertEquals(surname, response.surname());
        assertEquals(role, response.role());
        assertEquals("ACTIVE", response.status());
    }
}