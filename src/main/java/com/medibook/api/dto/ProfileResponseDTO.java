package com.medibook.api.dto;

import java.util.UUID;

public class ProfileResponseDTO {
    private UUID id;
    private String email;
    private String name;
    private String surname;


    private String medicalLicense;
    private String specialty;
    private int slotDurationMin;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getMedicalLicense() { return medicalLicense; }
    public void setMedicalLicense(String medicalLicense) { this.medicalLicense = medicalLicense; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public int getSlotDurationMin() { return slotDurationMin; }
    public void setSlotDurationMin(int slotDurationMin) { this.slotDurationMin = slotDurationMin; }
}
