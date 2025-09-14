package com.medibook.api.service;

import com.medibook.api.dto.DoctorDTO;
import com.medibook.api.entity.User;
import com.medibook.api.mapper.DoctorMapper;
import com.medibook.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorService {

    private final UserRepository userRepository;
    private final DoctorMapper doctorMapper;

    public List<DoctorDTO> getAllDoctors() {
        List<User> doctors = userRepository.findDoctorsByStatus("ACTIVE");
        return doctors.stream()
                .map(doctorMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<DoctorDTO> getDoctorsBySpecialty(String specialty) {
        List<User> doctors = userRepository.findDoctorsByStatusAndSpecialty("ACTIVE", specialty);
        return doctors.stream()
                .map(doctorMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<String> getAllSpecialties() {
        List<User> doctors = userRepository.findDoctorsByStatus("ACTIVE");
        return doctors.stream()
                .map(user -> user.getDoctorProfile().getSpecialty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}