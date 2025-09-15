package com.medibook.api.dto.Admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingDoctorDTO {
    private String id;
    private String name;
    private String surname;
    private String email;
    private String dni;
    private String gender;
    private String birthdate;
    private String phone;
    private String specialty;
    private String medicalLicense;
    private String role = "DOCTOR";
    private String status = "PENDING";
    private String createdAt;
    private String updatedAt;
}