package com.medibook.api.mapper;

import com.medibook.api.dto.Admin.PendingDoctorDTO;
import com.medibook.api.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AdminMapper {

    public PendingDoctorDTO convertToPendingDoctorDTO(User user) {
        PendingDoctorDTO dto = new PendingDoctorDTO();
        dto.setId(user.getId().toString());
        dto.setName(user.getName());
        dto.setSurname(user.getSurname());
        dto.setEmail(user.getEmail());
        dto.setDni(user.getDni().toString());
        dto.setGender(user.getGender());
        dto.setBirthdate(user.getBirthdate() != null ? user.getBirthdate().toString() : null);
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt().toString());
        
        if (user.getDoctorProfile() != null) {
            dto.setSpecialty(user.getDoctorProfile().getSpecialty());
            dto.setMedicalLicense(user.getDoctorProfile().getMedicalLicense());
        }
        
        return dto;
    }
}