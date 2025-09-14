package com.medibook.api.controller;

import com.medibook.api.dto.TurnCreateRequestDTO;
import com.medibook.api.dto.TurnReserveRequestDTO;
import com.medibook.api.dto.TurnResponseDTO;
import com.medibook.api.entity.TurnAssigned;
import com.medibook.api.service.TurnAssignedService;
import com.medibook.api.service.TurnAvailableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/turns")
@RequiredArgsConstructor
public class TurnAssignedController {

    private final TurnAssignedService turnService;
    private final TurnAvailableService availableService;

    @PostMapping
    public ResponseEntity<TurnResponseDTO> createTurn(@RequestBody TurnCreateRequestDTO dto) {
        TurnResponseDTO result = turnService.createTurn(dto);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @GetMapping("/available")
    public ResponseEntity<List<OffsetDateTime>> getAvailableTurns(
            @RequestParam UUID doctorId,
            @RequestParam String date) {
        
        LocalDate localDate = LocalDate.parse(date);
        LocalTime workStart = LocalTime.of(8, 0);
        LocalTime workEnd = LocalTime.of(18, 0);
        
        List<OffsetDateTime> available = availableService.getAvailableTurns(
                doctorId, localDate, workStart, workEnd);
        
        return ResponseEntity.ok(available);
    }

    @PostMapping("/reserve")
    public TurnAssigned reserveTurn(@RequestBody TurnReserveRequestDTO dto) {
        return turnService.reserveTurn(dto.getTurnId(), dto.getPatientId());
    }
}
