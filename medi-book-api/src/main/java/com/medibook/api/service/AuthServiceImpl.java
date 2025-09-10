package com.medibook.api.service;

import com.medibook.api.dto.RegisterRequestDTO;
import com.medibook.api.dto.RegisterResponseDTO;
import com.medibook.api.entity.User;
import com.medibook.api.mapper.UserMapper;
import com.medibook.api.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public RegisterResponseDTO registerPatient(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        String hashedPassword = passwordEncoder.encode(request.password());
        User user = userMapper.toUser(request, "PATIENT", hashedPassword);
        user = userRepository.save(user);

        return userMapper.toRegisterResponse(user);
    }

    @Override
    @Transactional
    public RegisterResponseDTO registerDoctor(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        if (request.medicalLicense() == null || request.specialty() == null) {
            throw new IllegalArgumentException("Medical license and specialty are required for doctors");
        }

        String hashedPassword = passwordEncoder.encode(request.password());
        User user = userMapper.toUser(request, "DOCTOR", hashedPassword);
        user = userRepository.save(user);

        return userMapper.toRegisterResponse(user);
    }
}