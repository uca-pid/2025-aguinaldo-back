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
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.doctorProfile dp WHERE u.role = 'DOCTOR' AND u.status = :status")
    List<User> findDoctorsByStatus(@Param("status") String status);
    
    @Query("SELECT u FROM User u JOIN FETCH u.doctorProfile dp WHERE u.role = 'DOCTOR' AND u.status = :status AND dp.specialty = :specialty")
    List<User> findDoctorsByStatusAndSpecialty(@Param("status") String status, @Param("specialty") String specialty);

    List<User> findByRoleAndStatusAndEmailVerified(String role, String status, Boolean email_verified);
    
    long countByRole(String role);
    long countByRoleAndStatus(String role, String status);
    long countByRoleAndEmailVerified(String role, boolean emailVerified);
    long countByRoleAndStatusAndEmailVerified(String role, String status, boolean emailVerified);
    
    @Query("SELECT u FROM User u JOIN FETCH u.doctorProfile WHERE u.role = 'DOCTOR' AND u.status = 'ACTIVE'")
    List<User> findAllDoctors();
    
    @Query("SELECT u FROM User u JOIN FETCH u.doctorProfile dp WHERE u.role = 'DOCTOR' AND u.status = 'ACTIVE' AND dp.specialty = :specialty")
    List<User> findDoctorsBySpecialty(@Param("specialty") String specialty);
}