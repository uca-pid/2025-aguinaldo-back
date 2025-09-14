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
    
    // Consolidated query for finding doctors with different criteria
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.doctorProfile dp WHERE u.role = 'DOCTOR' AND u.status = :status")
    List<User> findDoctorsByStatus(@Param("status") String status);
    
    @Query("SELECT u FROM User u JOIN FETCH u.doctorProfile dp WHERE u.role = 'DOCTOR' AND u.status = :status AND dp.specialty = :specialty")
    List<User> findDoctorsByStatusAndSpecialty(@Param("status") String status, @Param("specialty") String specialty);

    // Generic method for role and status queries
    List<User> findByRoleAndStatus(String role, String status);
    
    // Deprecated methods - replaced by more flexible alternatives above
    // Kept for backward compatibility during transition
    @Deprecated
    @Query("SELECT u FROM User u JOIN FETCH u.doctorProfile WHERE u.role = 'DOCTOR' AND u.status = 'ACTIVE'")
    List<User> findAllDoctors();
    
    @Deprecated
    @Query("SELECT u FROM User u JOIN FETCH u.doctorProfile dp WHERE u.role = 'DOCTOR' AND u.status = 'ACTIVE' AND dp.specialty = :specialty")
    List<User> findDoctorsBySpecialty(@Param("specialty") String specialty);
}