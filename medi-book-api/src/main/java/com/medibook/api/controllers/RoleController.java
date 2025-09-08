package com.medibook.api.controllers;

import com.medibook.api.dto.RoleDto;
import com.medibook.api.services.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleService service;

    public RoleController(RoleService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<RoleDto>> list() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{code}")
    public ResponseEntity<RoleDto> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(service.findByCode(code));
    }
}
