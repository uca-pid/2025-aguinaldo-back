package com.medibook.api.mappers;

import com.medibook.api.dto.RoleDto;
import com.medibook.api.entity.Role;

public final class RoleMapper {
    private RoleMapper() {}
    public static RoleDto toDto(Role r) {
        return new RoleDto(r.getId(), r.getCode(), r.getName());
    }
}

