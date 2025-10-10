package com.medibook.api.service;

import com.medibook.api.dto.Auth.RegisterRequestDTO;
import com.medibook.api.dto.Auth.RegisterResponseDTO;
import com.medibook.api.dto.Auth.SignInRequestDTO;
import com.medibook.api.dto.Auth.SignInResponseDTO;
import com.medibook.api.entity.RefreshToken;
import com.medibook.api.entity.User;
import com.medibook.api.mapper.AuthMapper;
import com.medibook.api.mapper.UserMapper;
import com.medibook.api.repository.RefreshTokenRepository;
import com.medibook.api.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.Base64;

@Service
@Slf4j
class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthMapper authMapper;
    private final EmailService emailService;

    public AuthServiceImpl(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper,
            AuthMapper authMapper,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.authMapper = authMapper;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public RegisterResponseDTO registerPatient(RegisterRequestDTO request) {
        validateCommonFields(request);
        
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }
        
        if (userRepository.existsByDni(request.dni())) {
            throw new IllegalArgumentException("DNI already registered");
        }

        String hashedPassword = passwordEncoder.encode(request.password());
        User user = userMapper.toUser(request, "PATIENT", hashedPassword);
        user = userRepository.save(user);
        
        try {
            final String userEmail = user.getEmail();
            final String userName = user.getName();
            
            emailService.sendWelcomeEmailToPatientAsync(userEmail, userName)
                .thenAccept(response -> {
                    if (response.isSuccess()) {
                        log.info("Email de bienvenida enviado al paciente: {}", userEmail);
                    } else {
                        log.warn("Falló email de bienvenida para {}: {}", userEmail, response.getMessage());
                    }
                })
                .exceptionally(throwable -> {
                    log.error("Error crítico enviando email a {}: {}", userEmail, throwable.getMessage());
                    return null;
                });
            
            log.info("Email de bienvenida encolado para: {}", userEmail);
        } catch (Exception e) {
            log.warn("Error encolando email de bienvenida para {}: {}", user.getEmail(), e.getMessage());            
        }

        return userMapper.toRegisterResponse(user);
    }

    @Override
    @Transactional
    public RegisterResponseDTO registerDoctor(RegisterRequestDTO request) {        
        validateCommonFields(request);
        validateDoctorFields(request);
        
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        if (userRepository.existsByDni(request.dni())) {
            throw new IllegalArgumentException("DNI already registered");
        }

        String hashedPassword = passwordEncoder.encode(request.password());
        User user = userMapper.toUser(request, "DOCTOR", hashedPassword);
        user.setStatus("PENDING");
        user = userRepository.save(user);

        return userMapper.toRegisterResponse(user);
    }

    @Override
    @Transactional
    public RegisterResponseDTO registerAdmin(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }
        
        if (userRepository.existsByDni(request.dni())) {
            throw new IllegalArgumentException("DNI already registered");
        }

        String hashedPassword = passwordEncoder.encode(request.password());
        User user = userMapper.toUser(request, "ADMIN", hashedPassword);
        user = userRepository.save(user);

        return userMapper.toRegisterResponse(user);
    }

    @Override
    @Transactional
    public SignInResponseDTO signIn(SignInRequestDTO request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!isUserAuthorizedToSignIn(user)) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        RefreshToken refreshToken = createRefreshToken(user);
        refreshTokenRepository.save(refreshToken);

        String accessToken = generateAccessToken(user);

        return authMapper.toSignInResponse(user, accessToken, refreshToken.getTokenHash());
    }

    private boolean isUserAuthorizedToSignIn(User user) {
        String role = user.getRole();
        String status = user.getStatus();
        
        if (role == null || status == null || role.trim().isEmpty() || status.trim().isEmpty()) {
            return false;
        }
        
        return switch (role.trim().toUpperCase()) {
            case "PATIENT", "ADMIN" -> "ACTIVE".equalsIgnoreCase(status.trim());
            case "DOCTOR" -> "ACTIVE".equalsIgnoreCase(status.trim()) || "PENDING".equalsIgnoreCase(status.trim());
            default -> false;
        };
    }

    @Override
    @Transactional
    public void signOut(String refreshTokenHash) {
        refreshTokenRepository.revokeTokenByHash(refreshTokenHash, ZonedDateTime.now());
    }

    @Override
    @Transactional
    public SignInResponseDTO refreshToken(String refreshTokenHash) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(refreshTokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (!refreshToken.isValid()) {
            throw new IllegalArgumentException("Refresh token is expired or revoked");
        }

        User user = refreshToken.getUser();

        String newAccessToken = generateAccessToken(user);

        RefreshToken newRefreshToken = createRefreshToken(user);
        refreshTokenRepository.save(newRefreshToken);
        refreshTokenRepository.revokeTokenByHash(refreshTokenHash, ZonedDateTime.now());

        return authMapper.toSignInResponse(user, newAccessToken, newRefreshToken.getTokenHash());
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(generateSecureToken());
        refreshToken.setExpiresAt(ZonedDateTime.now().plusDays(30));
        refreshToken.setCreatedAt(ZonedDateTime.now());
        return refreshToken;
    }

    private String generateSecureToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private String generateAccessToken(User user) {
        return "jwt-token-for-user-" + user.getId();
    }

    private void validateCommonFields(RegisterRequestDTO request) {
        if (request.birthdate() == null) {
            throw new IllegalArgumentException("Birthdate is required");
        }
        
        if (request.gender() == null || request.gender().trim().isEmpty()) {
            throw new IllegalArgumentException("Gender is required");
        }
        
        if (request.phone() == null || request.phone().trim().isEmpty()) {
            throw new IllegalArgumentException("Phone is required");
        }
                
        if (request.dni() != null) {
            String dniStr = request.dni().toString();
            if (!dniStr.matches("^[0-9]{7,8}$")) {
                throw new IllegalArgumentException("Invalid DNI format (7-8 digits)");
            }
            if (request.dni() < 1000000L || request.dni() > 999999999L) {
                throw new IllegalArgumentException("DNI out of valid range");
            }
        }
                
        if (request.birthdate().isAfter(java.time.LocalDate.now().minusYears(18))) {
            throw new IllegalArgumentException("Must be at least 18 years old");
        }
        
        if (request.birthdate().isBefore(java.time.LocalDate.now().minusYears(120))) {
            throw new IllegalArgumentException("Invalid birth date");
        }
    }

    private void validateDoctorFields(RegisterRequestDTO request) {
        if (request.medicalLicense() == null || request.medicalLicense().trim().isEmpty()) {
            throw new IllegalArgumentException("Medical license is required for doctors");
        }
        
        if (request.specialty() == null || request.specialty().trim().isEmpty()) {
            throw new IllegalArgumentException("Specialty is required for doctors");
        }
        
        if (request.slotDurationMin() == null) {
            throw new IllegalArgumentException("Slot duration is required for doctors");
        }
                
        if (!request.medicalLicense().matches("^[0-9]{4,10}$")) {
            throw new IllegalArgumentException("Medical license must be 4-10 digits");
        }
                
        if (!request.specialty().matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]+$")) {
            throw new IllegalArgumentException("Specialty can only contain letters and spaces");
        }
                
        if (request.specialty().length() < 2) {
            throw new IllegalArgumentException("Specialty minimum 2 characters");
        }
        
        if (request.specialty().length() > 50) {
            throw new IllegalArgumentException("Specialty maximum 50 characters");
        }
                
        if (request.slotDurationMin() < 5 || request.slotDurationMin() > 180) {
            throw new IllegalArgumentException("Slot duration must be between 5 and 180 minutes");
        }
    }
}