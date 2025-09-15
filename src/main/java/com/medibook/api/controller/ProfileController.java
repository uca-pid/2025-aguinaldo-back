package com.medibook.api.controller;

import com.medibook.api.dto.ProfileResponseDTO;
import com.medibook.api.service.ProfileService;
import com.medibook.api.service.AuthenticatedUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final AuthenticatedUserService authenticatedUserService;

    public ProfileController(ProfileService profileService,
                             AuthenticatedUserService authenticatedUserService) {
        this.profileService = profileService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @GetMapping ("/{userId}")
    public ResponseEntity<ProfileResponseDTO> getProfile(UUID userId) {
       
        ProfileResponseDTO profile = profileService.getProfile(userId);
        return ResponseEntity.ok(profile);
    }
}
