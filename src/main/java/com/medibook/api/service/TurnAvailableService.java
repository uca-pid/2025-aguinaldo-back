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

    public List<OffsetDateTime> getAvailableTurns(UUID doctorId, LocalDate date, LocalTime workStart, LocalTime workEnd) {
        List<TurnAssigned> turns = turnRepo.findByDoctor_IdAndScheduledAtBetween(
                doctorId,
                date.atTime(workStart).atOffset(ZoneOffset.UTC),
                date.atTime(workEnd).atOffset(ZoneOffset.UTC)
        );

        List<OffsetDateTime> available = new ArrayList<>();
        Duration slotDuration = Duration.ofMinutes(15); // o tomarlo del DoctorProfile

        OffsetDateTime current = date.atTime(workStart).atOffset(ZoneOffset.UTC);
        OffsetDateTime end = date.atTime(workEnd).atOffset(ZoneOffset.UTC);

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
