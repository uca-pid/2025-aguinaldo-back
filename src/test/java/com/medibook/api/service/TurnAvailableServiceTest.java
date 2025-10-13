package com.medibook.api.service;

import com.medibook.api.entity.TurnAssigned;
import com.medibook.api.entity.User;
import com.medibook.api.repository.TurnAssignedRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TurnAvailableServiceTest {

    @Mock
    private TurnAssignedRepository turnRepo;

    @InjectMocks
    private TurnAvailableService turnAvailableService;

    private UUID doctorId;
    private LocalDate testDate;
    private LocalTime workStart;
    private LocalTime workEnd;
    private User doctor;
    private static final ZoneId ARGENTINA_ZONE = ZoneId.of("America/Argentina/Buenos_Aires");

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();
        testDate = LocalDate.of(2025, 9, 15);
        workStart = LocalTime.of(8, 0);
        workEnd = LocalTime.of(18, 0);

        doctor = new User();
        doctor.setId(doctorId);
        doctor.setName("Dr. Hugo");
        doctor.setSurname("Martinez");
        doctor.setRole("DOCTOR");
    }

    @Test
    void getAvailableTurns_NoOccupiedSlots_ReturnsAllSlots() {
        ZoneOffset argentinaOffset = ARGENTINA_ZONE.getRules().getOffset(testDate.atStartOfDay());
        
        when(turnRepo.findByDoctor_IdAndScheduledAtBetween(
                eq(doctorId),
                eq(testDate.atTime(workStart).atOffset(argentinaOffset)),
                eq(testDate.atTime(workEnd).atOffset(argentinaOffset))
        )).thenReturn(Arrays.asList());

        List<OffsetDateTime> availableSlots = turnAvailableService.getAvailableTurns(
                doctorId, testDate, workStart, workEnd
        );

        assertNotNull(availableSlots);
        assertEquals(40, availableSlots.size());
        
        OffsetDateTime firstSlot = testDate.atTime(workStart).atOffset(argentinaOffset);
        assertEquals(firstSlot, availableSlots.get(0));
        
        OffsetDateTime lastSlot = testDate.atTime(workEnd).atOffset(argentinaOffset).minusMinutes(15);
        assertEquals(lastSlot, availableSlots.get(availableSlots.size() - 1));

        verify(turnRepo).findByDoctor_IdAndScheduledAtBetween(any(), any(), any());
    }

    @Test
    void getAvailableTurns_WithOccupiedSlots_ExcludesOccupiedSlots() {
        ZoneOffset argentinaOffset = ARGENTINA_ZONE.getRules().getOffset(testDate.atStartOfDay());
        
        OffsetDateTime occupiedSlot1 = testDate.atTime(10, 0).atOffset(argentinaOffset);
        OffsetDateTime occupiedSlot2 = testDate.atTime(14, 30).atOffset(argentinaOffset);

        TurnAssigned turn1 = TurnAssigned.builder()
                .id(UUID.randomUUID())
                .doctor(doctor)
                .scheduledAt(occupiedSlot1)
                .status("SCHEDULED")
                .build();

        TurnAssigned turn2 = TurnAssigned.builder()
                .id(UUID.randomUUID())
                .doctor(doctor)
                .scheduledAt(occupiedSlot2)
                .status("SCHEDULED")
                .build();

        when(turnRepo.findByDoctor_IdAndScheduledAtBetween(
                eq(doctorId),
                eq(testDate.atTime(workStart).atOffset(argentinaOffset)),
                eq(testDate.atTime(workEnd).atOffset(argentinaOffset))
        )).thenReturn(Arrays.asList(turn1, turn2));

        List<OffsetDateTime> availableSlots = turnAvailableService.getAvailableTurns(
                doctorId, testDate, workStart, workEnd
        );

        assertNotNull(availableSlots);
        assertEquals(38, availableSlots.size());
        
        assertFalse(availableSlots.contains(occupiedSlot1));
        assertFalse(availableSlots.contains(occupiedSlot2));

        verify(turnRepo).findByDoctor_IdAndScheduledAtBetween(any(), any(), any());
    }

    @Test
    void getAvailableTurns_AllSlotsOccupied_ReturnsEmptyList() {
        ZoneOffset argentinaOffset = ARGENTINA_ZONE.getRules().getOffset(testDate.atStartOfDay());
        List<TurnAssigned> allSlots = generateAllPossibleTurns(argentinaOffset);
        
        when(turnRepo.findByDoctor_IdAndScheduledAtBetween(
                eq(doctorId),
                eq(testDate.atTime(workStart).atOffset(argentinaOffset)),
                eq(testDate.atTime(workEnd).atOffset(argentinaOffset))
        )).thenReturn(allSlots);

        List<OffsetDateTime> availableSlots = turnAvailableService.getAvailableTurns(
                doctorId, testDate, workStart, workEnd
        );

        assertNotNull(availableSlots);
        assertEquals(0, availableSlots.size());

        verify(turnRepo).findByDoctor_IdAndScheduledAtBetween(any(), any(), any());
    }

    @Test
    void getAvailableTurns_EmptyWorkDay_ReturnsEmptyList() {
        LocalTime sameTime = LocalTime.of(9, 0);
        
        when(turnRepo.findByDoctor_IdAndScheduledAtBetween(any(), any(), any()))
                .thenReturn(Arrays.asList());

        List<OffsetDateTime> availableSlots = turnAvailableService.getAvailableTurns(
                doctorId, testDate, sameTime, sameTime
        );

        assertNotNull(availableSlots);
        assertEquals(0, availableSlots.size());

        verify(turnRepo).findByDoctor_IdAndScheduledAtBetween(any(), any(), any());
    }

    @Test
    void getAvailableTurns_VerifySlotInterval() {
        when(turnRepo.findByDoctor_IdAndScheduledAtBetween(any(), any(), any()))
                .thenReturn(Arrays.asList());

        List<OffsetDateTime> availableSlots = turnAvailableService.getAvailableTurns(
                doctorId, testDate, workStart, workEnd
        );

        assertNotNull(availableSlots);
        assertTrue(availableSlots.size() > 1);
        
        for (int i = 1; i < availableSlots.size(); i++) {
            OffsetDateTime previous = availableSlots.get(i - 1);
            OffsetDateTime current = availableSlots.get(i);
            
            Duration difference = Duration.between(previous, current);
            assertEquals(Duration.ofMinutes(15), difference);
        }
    }

    @Test
    void getAvailableTurns_VerifyTimeRange() {
        ZoneOffset argentinaOffset = ARGENTINA_ZONE.getRules().getOffset(testDate.atStartOfDay());
        
        when(turnRepo.findByDoctor_IdAndScheduledAtBetween(any(), any(), any()))
                .thenReturn(Arrays.asList());

        List<OffsetDateTime> availableSlots = turnAvailableService.getAvailableTurns(
                doctorId, testDate, workStart, workEnd
        );

        assertNotNull(availableSlots);
        
        OffsetDateTime workStartDateTime = testDate.atTime(workStart).atOffset(argentinaOffset);
        OffsetDateTime workEndDateTime = testDate.atTime(workEnd).atOffset(argentinaOffset);
        
        for (OffsetDateTime slot : availableSlots) {
            assertTrue(slot.isAfter(workStartDateTime) || slot.isEqual(workStartDateTime));
            assertTrue(slot.isBefore(workEndDateTime));
        }
    }

    private List<TurnAssigned> generateAllPossibleTurns(ZoneOffset offset) {
        List<TurnAssigned> turns = new java.util.ArrayList<>();
        OffsetDateTime current = testDate.atTime(workStart).atOffset(offset);
        OffsetDateTime end = testDate.atTime(workEnd).atOffset(offset);
        
        while (current.isBefore(end)) {
            TurnAssigned turn = TurnAssigned.builder()
                    .id(UUID.randomUUID())
                    .doctor(doctor)
                    .scheduledAt(current)
                    .status("SCHEDULED")
                    .build();
            turns.add(turn);
            current = current.plusMinutes(15);
        }
        
        return turns;
    }
}