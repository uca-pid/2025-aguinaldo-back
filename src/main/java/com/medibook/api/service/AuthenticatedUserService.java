package com.medibook.api.service;

import com.medibook.api.entity.User;
import com.medibook.api.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthenticatedUserService {
    
    private final UserRepository userRepository;
    
    public AuthenticatedUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public Optional<User> validateAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isEmpty()) {
            return Optional.empty();
        }
        
        if (!accessToken.startsWith("jwt-token-for-user-")) {
            return Optional.empty();
        }
        
        String userIdString = accessToken.substring("jwt-token-for-user-".length());
        
        try {
            UUID userId = UUID.fromString(userIdString);
            Optional<User> userOpt = userRepository.findById(userId);
            
            if (userOpt.isPresent() && "ACTIVE".equals(userOpt.get().getStatus())) {
                return userOpt;
            }
            return Optional.empty();
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
    
    public Optional<User> getUserFromAuthorizationHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }
        
        String accessToken = authorizationHeader.substring("Bearer ".length());
        return validateAccessToken(accessToken);
    }

}