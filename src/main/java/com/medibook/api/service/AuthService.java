package com.medibook.api.service;

import com.medibook.api.dto.Auth.RegisterRequestDTO;
import com.medibook.api.dto.Auth.RegisterResponseDTO;
import com.medibook.api.dto.Auth.SignInRequestDTO;
import com.medibook.api.dto.Auth.SignInResponseDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public interface AuthService {
    RegisterResponseDTO registerPatient(RegisterRequestDTO request);
    RegisterResponseDTO registerDoctor(RegisterRequestDTO request);
    RegisterResponseDTO registerAdmin(RegisterRequestDTO request);
    
    SignInResponseDTO signIn(SignInRequestDTO request);
    void signOut(String refreshToken);
    SignInResponseDTO refreshToken(String refreshToken);
}