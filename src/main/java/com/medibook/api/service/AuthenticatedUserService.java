package com.medibook.api.service;

import com.medibook.api.entity.User;
import com.medibook.api.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthenticatedUserService {
    
    private final UserRepository userRepository;
    private final JwtService jwtService;
    
    public AuthenticatedUserService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }
    
    public Optional<User> validateAccessToken(String accessToken) {
        jwtService.validateTokenThrows(accessToken);     

        String userIdString = jwtService.extractUserId(accessToken);

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