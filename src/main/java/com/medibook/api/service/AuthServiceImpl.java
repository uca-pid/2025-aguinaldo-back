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

import static com.medibook.api.util.DateTimeUtils.ARGENTINA_ZONE;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.security.MessageDigest;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.nio.charset.StandardCharsets;


@Service
@Slf4j
class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthMapper authMapper;
    private final EmailService emailService;
    private final JwtService jwtService;

    private static final java.util.Set<String> VALID_SPECIALTIES = java.util.Set.of(
        "ALERGIA E INMUNOLOGÍA",
        "ANATOMÍA PATOLÓGICA",
        "ANESTESIOLOGÍA",
        "ANGIOLOGÍA GENERAL y HEMODINAMIA",
        "CARDIOLOGÍA",
        "CARDIÓLOGO INFANTIL",
        "CIRUGÍA GENERAL",
        "CIRUGÍA CARDIOVASCULAR",
        "CIRUGÍA DE CABEZA Y CUELLO",
        "CIRUGÍA DE TÓRAX (CIRUGÍA TORÁCICA)",
        "CIRUGÍA INFANTIL (CIRUGÍA PEDIÁTRICA)",
        "CIRUGÍA PLÁSTICA Y REPARADORA",
        "CIRUGÍA VASCULAR PERIFÉRICA",
        "CLÍNICA MÉDICA",
        "COLOPROCTOLOGÍA",
        "DERMATOLOGÍA",
        "DIAGNOSTICO POR IMÁGENES",
        "ENDOCRINOLOGÍA",
        "ENDOCRINÓLOGO INFANTIL",
        "FARMACOLOGÍA CLÍNICA",
        "FISIATRÍA (MEDICINA FÍSICA Y REHABILITACIÓN)",
        "GASTROENTEROLOGÍA",
        "GASTROENTERÓLOGO INFANTIL",
        "GENÉTICA MEDICA",
        "GERIATRÍA",
        "GINECOLOGÍA",
        "HEMATOLOGÍA",
        "HEMATÓLOGO INFANTIL",
        "HEMOTERAPIA E INMUNOHEMATOLOGÍA",
        "INFECTOLOGÍA",
        "INFECTÓLOGO INFANTIL",
        "MEDICINA DEL DEPORTE",
        "MEDICINA GENERAL y/o MEDICINA DE FAMILIA",
        "MEDICINA LEGAL",
        "MEDICINA NUCLEAR",
        "MEDICINA DEL TRABAJO",
        "NEFROLOGÍA",
        "NEFRÓLOGO INFANTIL",
        "NEONATOLOGÍA",
        "NEUMONOLOGÍA",
        "NEUMONÓLOGO INFANTIL",
        "NEUROCIRUGÍA",
        "NEUROLOGÍA",
        "NEURÓLOGO INFANTIL",
        "NUTRICIÓN",
        "OBSTETRICIA",
        "OFTALMOLOGÍA",
        "ONCOLOGÍA",
        "ONCÓLOGO INFANTIL",
        "ORTOPEDIA Y TRAUMATOLOGÍA",
        "OTORRINOLARINGOLOGÍA",
        "PEDIATRÍA",
        "PSIQUIATRÍA",
        "PSIQUIATRÍA INFANTO JUVENIL",
        "RADIOTERAPIA O TERAPIA RADIANTE",
        "REUMATOLOGÍA",
        "REUMATÓLOGO INFANTIL",
        "TERAPIA INTENSIVA",
        "TERAPISTA INTENSIVO INFANTIL",
        "TOCOGINECOLOGÍA",
        "TOXICOLOGÍA",
        "UROLOGÍA"
    );

    public AuthServiceImpl(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper,
            AuthMapper authMapper,
            EmailService emailService,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.authMapper = authMapper;
        this.emailService = emailService;
        this.jwtService = jwtService;
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

        String rawToken = generateSecureToken();
        String hashedToken = hashToken(rawToken);

        RefreshToken refreshToken = createRefreshToken(user, hashedToken);
        refreshTokenRepository.save(refreshToken);

        String accessToken = generateAccessToken(user);

        return authMapper.toSignInResponse(user, accessToken, rawToken);
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
    public void signOut(String rawRefreshToken) {
        if (rawRefreshToken == null) {
            return;
        }
        String hashedToken = hashToken(rawRefreshToken);
        refreshTokenRepository.revokeTokenByHash(hashedToken, ZonedDateTime.now(ARGENTINA_ZONE));
    }

    @Override
    @Transactional
    public SignInResponseDTO refreshToken(String rawRefreshToken) {
        
        String hashedInputToken = hashToken(rawRefreshToken);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hashedInputToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (!refreshToken.isValid()) {
            throw new IllegalArgumentException("Refresh token is expired or revoked");
        }

        User user = refreshToken.getUser();
        String newAccessToken = generateAccessToken(user);

        String newRawToken = generateSecureToken();
        String newHashedToken = hashToken(newRawToken);

        RefreshToken newRefreshToken = createRefreshToken(user, newHashedToken);
        refreshTokenRepository.save(newRefreshToken);
        
        refreshTokenRepository.revokeTokenByHash(hashedInputToken, ZonedDateTime.now(ARGENTINA_ZONE));

        return authMapper.toSignInResponse(user, newAccessToken, newRawToken);
    }

    private RefreshToken createRefreshToken(User user, String hashedToken) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(hashedToken);
        refreshToken.setExpiresAt(ZonedDateTime.now(ARGENTINA_ZONE).plusDays(30));
        refreshToken.setCreatedAt(ZonedDateTime.now(ARGENTINA_ZONE));
        return refreshToken;
    }

    private String generateSecureToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private String generateAccessToken(User user) {
        return jwtService.generateToken(user);
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
                
        if (request.birthdate().isAfter(LocalDate.now(ARGENTINA_ZONE).minusYears(18))) {
            throw new IllegalArgumentException("Must be at least 18 years old");
        }
        
        if (request.birthdate().isBefore(LocalDate.now(ARGENTINA_ZONE).minusYears(120))) {
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
        
        if (!VALID_SPECIALTIES.contains(request.specialty().trim())) {
            throw new IllegalArgumentException("Invalid specialty selected");
        }
        
        if (request.slotDurationMin() == null) {
            throw new IllegalArgumentException("Slot duration is required for doctors");
        }
                
        if (!request.medicalLicense().matches("^[0-9]{4,10}$")) {
            throw new IllegalArgumentException("Medical license must be 4-10 digits");
        }
                
        if (request.slotDurationMin() < 5 || request.slotDurationMin() > 180) {
            throw new IllegalArgumentException("Slot duration must be between 5 and 180 minutes");
        }
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("Error initializing SHA-256", e);
        }
    }
}