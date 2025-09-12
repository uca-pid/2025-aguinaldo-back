package com.medibook.api.service;

import com.medibook.api.dto.RegisterRequestDTO;
import com.medibook.api.dto.RegisterResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public interface AuthService {
    RegisterResponseDTO registerPatient(RegisterRequestDTO request);
    RegisterResponseDTO registerDoctor(RegisterRequestDTO request);
}