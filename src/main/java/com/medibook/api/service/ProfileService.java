package com.medibook.api.service;

import com.medibook.api.dto.ProfileResponseDTO;
import com.medibook.api.dto.ProfileUpdateRequestDTO;
import com.medibook.api.entity.User;
import com.medibook.api.repository.UserRepository;
import com.medibook.api.repository.RefreshTokenRepository;
import com.medibook.api.mapper.ProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

    private final UserRepository userRepository;
    private final ProfileMapper profileMapper;
    private final RefreshTokenRepository refreshTokenRepository;

    public ProfileResponseDTO getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return profileMapper.toProfileResponse(user);
    }

    @Transactional
    public ProfileResponseDTO updateProfile(UUID userId, ProfileUpdateRequestDTO updateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (updateRequest.email() != null && !updateRequest.email().isBlank() && 
            !user.getEmail().equals(updateRequest.email())) {
            throw new IllegalArgumentException("Email already in use");
        }

        profileMapper.updateUserFromRequest(user, updateRequest);
        user = userRepository.save(user);
        
        return profileMapper.toProfileResponse(user);
    }

    @Transactional
    public void deactivateUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        System.out.println("Desactivando usuario: " + user.getEmail() + " - Estado anterior: " + user.getStatus());
        
        user.setStatus("DISABLED");
        userRepository.save(user);
        
        refreshTokenRepository.revokeAllTokensByUserId(userId, ZonedDateTime.now());
        
        System.out.println("Usuario desactivado: " + user.getEmail() + " - Estado actual: " + user.getStatus());
        System.out.println("Tokens revocados para usuario: " + user.getEmail());
    }
}
