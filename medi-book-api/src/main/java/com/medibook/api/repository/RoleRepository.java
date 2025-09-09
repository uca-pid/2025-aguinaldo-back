package com.medibook.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.medibook.api.entity.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Short> {
    Optional<Role> findByCode(String code);
}
