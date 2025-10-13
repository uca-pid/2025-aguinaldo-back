package com.medibook.api.service;

import com.medibook.api.entity.TurnAssigned;
import com.medibook.api.repository.TurnAssignedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TurnAvailableService {

    private final TurnAssignedRepository turnRepo;
    private static final ZoneId ARGENTINA_ZONE = ZoneId.of("America/Argentina/Buenos_Aires");

    public List<OffsetDateTime> getAvailableTurns(UUID doctorId, LocalDate date, LocalTime workStart, LocalTime workEnd) {
        ZoneOffset argentinaOffset = ARGENTINA_ZONE.getRules().getOffset(date.atStartOfDay());
        
        List<TurnAssigned> turns = turnRepo.findByDoctor_IdAndScheduledAtBetween(
                doctorId,
                date.atTime(workStart).atOffset(argentinaOffset),
                date.atTime(workEnd).atOffset(argentinaOffset)
        );

        List<OffsetDateTime> available = new ArrayList<>();
        Duration slotDuration = Duration.ofMinutes(15); 

        OffsetDateTime current = date.atTime(workStart).atOffset(argentinaOffset);
        OffsetDateTime end = date.atTime(workEnd).atOffset(argentinaOffset);

        while (current.isBefore(end)) {
            OffsetDateTime iter = current;
            boolean occupied = turns.stream().anyMatch(t -> t.getScheduledAt().equals(iter));
            if (!occupied) {
                available.add(current);
            }
            current = current.plus(slotDuration);
        }

        return available;
    }
}