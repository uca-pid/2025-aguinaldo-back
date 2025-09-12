package com.medibook.api.repository;

import com.medibook.api.entity.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, UUID> {
}