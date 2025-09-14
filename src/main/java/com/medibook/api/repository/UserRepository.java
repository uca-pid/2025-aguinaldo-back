package com.medibook.api.repository;

import com.medibook.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmail(String email);

    boolean existsByDni(Long dni);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndStatus(String email, String status);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email AND u.status = 'ACTIVE'")
    boolean isUserActive(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.role = 'DOCTOR' AND u.status = :status ORDER BY u.createdAt ASC")
    List<User> findDoctorsByStatusOrderByCreatedAtAsc(@Param("status") String status);

    @Query("SELECT u FROM User u WHERE u.role = 'DOCTOR' ORDER BY u.createdAt DESC")
    List<User> findAllDoctorsOrderByCreatedAtDesc();

    Optional<User> findByIdAndRoleAndStatus(UUID id, String role, String status);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.doctorProfile.medicalLicense = :medicalLicense")
    boolean existsByMedicalLicense(@Param("medicalLicense") String medicalLicense);
}