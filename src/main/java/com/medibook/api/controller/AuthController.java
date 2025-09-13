package com.medibook.api.controller;

import com.medibook.api.dto.RegisterRequestDTO;
import com.medibook.api.dto.RegisterResponseDTO;
import com.medibook.api.dto.SignInRequestDTO;
import com.medibook.api.dto.SignInResponseDTO;
import com.medibook.api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/patient")
    public ResponseEntity<RegisterResponseDTO> registerPatient(
            @Valid @RequestBody RegisterRequestDTO request) {
        RegisterResponseDTO response = authService.registerPatient(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/register/doctor")
    public ResponseEntity<RegisterResponseDTO> registerDoctor(
            @Valid @RequestBody RegisterRequestDTO request) {
        RegisterResponseDTO response = authService.registerDoctor(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/signin")
    public ResponseEntity<SignInResponseDTO> signIn(
            @Valid @RequestBody SignInRequestDTO request) {
        SignInResponseDTO response = authService.signIn(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signout")
    public ResponseEntity<Void> signOut(
            @RequestHeader("Refresh-Token") String refreshToken) {
        authService.signOut(refreshToken);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<SignInResponseDTO> refreshToken(
            @RequestHeader("Refresh-Token") String refreshToken) {
        SignInResponseDTO response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}