package com.medibook.api.mapper;

import com.medibook.api.dto.DoctorDTO;
import com.medibook.api.entity.User;
import org.springframework.stereotype.Component;

@Component
public class DoctorMapper {

    public DoctorDTO toDTO(User user) {
        if (user == null || user.getDoctorProfile() == null) {
            return null;
        }

        return DoctorDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .email(user.getEmail())
                .medicalLicense(user.getDoctorProfile().getMedicalLicense())
                .specialty(user.getDoctorProfile().getSpecialty())
                .slotDurationMin(user.getDoctorProfile().getSlotDurationMin())
                .score(user.getScore())
                .build();
    }
}