
package com.medibook.api.services;

import com.medibook.api.dto.RoleDto;
import com.medibook.api.mappers.RoleMapper;
import com.medibook.api.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RoleService {
    private final RoleRepository repo;

    public RoleService(RoleRepository repo) {
        this.repo = repo;
    }

    public List<RoleDto> findAll() {
        return repo.findAll().stream().map(RoleMapper::toDto).toList();
    }

    public RoleDto findByCode(String code) {
        return repo.findByCode(code)
                .map(RoleMapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + code));
    }
}

