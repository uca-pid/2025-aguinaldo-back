package com.medibook.api.service;

import com.medibook.api.dto.ProfileResponseDTO;
import com.medibook.api.entity.User;
import com.medibook.api.entity.DoctorProfile;
import com.medibook.api.repository.UserRepository;
import com.medibook.api.repository.DoctorProfileRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;

    public ProfileService(UserRepository userRepository,
                          DoctorProfileRepository doctorProfileRepository) {
        this.userRepository = userRepository;
        this.doctorProfileRepository = doctorProfileRepository;
    }

    public ProfileResponseDTO getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        ProfileResponseDTO dto = new ProfileResponseDTO();
            dto.setId(user.getId());
            dto.setEmail(user.getEmail());
            dto.setName(user.getName());
            dto.setSurname(user.getSurname());

        if (user.getRole() != null && "DOCTOR".equalsIgnoreCase(user.getRole())) {
            DoctorProfile doctorProfile = doctorProfileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Perfil de doctor no encontrado"));

            dto.setMedicalLicense(doctorProfile.getMedicalLicense());
            dto.setSpecialty(doctorProfile.getSpecialty());
            dto.setSlotDurationMin(doctorProfile.getSlotDurationMin());
        }

        return dto;
    }
}
