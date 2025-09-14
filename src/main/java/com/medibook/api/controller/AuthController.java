package com.medibook.api.controller;

import com.medibook.api.dto.ErrorResponseDTO;
import com.medibook.api.dto.RegisterRequestDTO;
import com.medibook.api.dto.RegisterResponseDTO;
import com.medibook.api.dto.SignInRequestDTO;
import com.medibook.api.dto.SignInResponseDTO;
import com.medibook.api.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/patient")
    public ResponseEntity<?> registerPatient(
            @Valid @RequestBody RegisterRequestDTO request, 
            HttpServletRequest httpRequest) {
        try {
            RegisterResponseDTO response = authService.registerPatient(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            ErrorResponseDTO error = ErrorResponseDTO.of(
                "REGISTRATION_FAILED", 
                e.getMessage(), 
                HttpStatus.BAD_REQUEST.value(),
                httpRequest.getRequestURI()
            );
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/register/doctor")
    public ResponseEntity<?> registerDoctor(
            @Valid @RequestBody RegisterRequestDTO request, 
            HttpServletRequest httpRequest) {
        try {
            RegisterResponseDTO response = authService.registerDoctor(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            ErrorResponseDTO error = ErrorResponseDTO.of(
                "DOCTOR_REGISTRATION_FAILED", 
                e.getMessage(), 
                HttpStatus.BAD_REQUEST.value(),
                httpRequest.getRequestURI()
            );
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/register/admin")
    public ResponseEntity<?> registerAdmin(
            @Valid @RequestBody RegisterRequestDTO request, 
            HttpServletRequest httpRequest) {
        try {
            RegisterResponseDTO response = authService.registerAdmin(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            ErrorResponseDTO error = ErrorResponseDTO.of(
                "ADMIN_REGISTRATION_FAILED", 
                e.getMessage(), 
                HttpStatus.BAD_REQUEST.value(),
                httpRequest.getRequestURI()
            );
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(
            @Valid @RequestBody SignInRequestDTO request, 
            HttpServletRequest httpRequest) {
        try {
            SignInResponseDTO response = authService.signIn(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            String errorCode = determineSignInErrorCode(e.getMessage());
            ErrorResponseDTO error = ErrorResponseDTO.of(
                errorCode, 
                e.getMessage(), 
                HttpStatus.UNAUTHORIZED.value(),
                httpRequest.getRequestURI()
            );
            return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/signout")
    public ResponseEntity<?> signOut(
            @RequestHeader("Refresh-Token") String refreshToken,
            HttpServletRequest httpRequest) {
        try {
            authService.signOut(refreshToken);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            ErrorResponseDTO error = ErrorResponseDTO.of(
                "SIGNOUT_FAILED", 
                e.getMessage(), 
                HttpStatus.BAD_REQUEST.value(),
                httpRequest.getRequestURI()
            );
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(
            @RequestHeader("Refresh-Token") String refreshToken,
            HttpServletRequest httpRequest) {
        try {
            SignInResponseDTO response = authService.refreshToken(refreshToken);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            ErrorResponseDTO error = ErrorResponseDTO.of(
                "TOKEN_REFRESH_FAILED", 
                e.getMessage(), 
                HttpStatus.UNAUTHORIZED.value(),
                httpRequest.getRequestURI()
            );
            return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }
    }

    private String determineSignInErrorCode(String message) {
        if (message.contains("Invalid email or password")) {
            return "INVALID_CREDENTIALS";
        }
        if (message.contains("not found") || message.contains("ACTIVE")) {
            return "ACCOUNT_NOT_ACTIVE";
        }
        return "SIGNIN_FAILED";
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });
        
        String message = "Validation failed: " + validationErrors.toString();
        ErrorResponseDTO error = ErrorResponseDTO.of(
            "VALIDATION_ERROR", 
            message, 
            HttpStatus.BAD_REQUEST.value(),
            request.getRequestURI()
        );
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        
        ErrorResponseDTO error = ErrorResponseDTO.of(
            "BAD_REQUEST", 
            ex.getMessage(), 
            HttpStatus.BAD_REQUEST.value(),
            request.getRequestURI()
        );
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        ErrorResponseDTO error = ErrorResponseDTO.of(
            "INTERNAL_SERVER_ERROR", 
            "An unexpected error occurred", 
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            request.getRequestURI()
        );
        
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}