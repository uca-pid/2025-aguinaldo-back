package com.medibook.api.repository;

import com.medibook.api.entity.DoctorBadgeStatistics;
import com.medibook.api.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class DoctorBadgeStatisticsRepositoryTest {

    @Autowired
    private DoctorBadgeStatisticsRepository statisticsRepository;

    @Autowired
    private TestEntityManager entityManager;

    private static long dniCounter = 12345678L;

    private User createTestDoctor() {
        User doctor = new User();
        doctor.setEmail("doctor" + dniCounter + "@test.com");
        doctor.setDni(dniCounter++);
        doctor.setPasswordHash("hash");
        doctor.setName("Test");
        doctor.setSurname("Doctor");
        return entityManager.persistAndFlush(doctor);
    }

    private DoctorBadgeStatistics createTestStatistics(User doctor) {
        DoctorBadgeStatistics stats = DoctorBadgeStatistics.builder()
                .doctor(doctor)
                .totalRatingsReceived(50)
                .last50CommunicationCount(10)
                .last50EmpathyCount(12)
                .last50PunctualityCount(8)
                .totalTurnsCompleted(100)
                .totalTurnsCancelled(5)
                .turnsLast90Days(20)
                .cancellationsLast90Days(2)
                .last50DocumentedCount(45)
                .last30DocumentedCount(25)
                .last30TotalWords(4000)
                .last30AvgWordsPerEntry(160.0)
                .totalUniquePatients(50)
                .returningPatientsCount(8)
                .last10RequestsHandled(5)
                .lastUpdatedAt(OffsetDateTime.now().minusDays(1))
                .build();
        return entityManager.persistAndFlush(stats);
    }

    @Test
    void findByDoctorId_ReturnsStatisticsWhenExists() {
        User doctor = createTestDoctor();
        DoctorBadgeStatistics stats = createTestStatistics(doctor);

        Optional<DoctorBadgeStatistics> foundStats = statisticsRepository.findByDoctorId(doctor.getId());

        assertTrue(foundStats.isPresent());
        assertEquals(stats.getDoctorId(), foundStats.get().getDoctorId());
        assertEquals(50, foundStats.get().getTotalRatingsReceived());
        assertEquals(100, foundStats.get().getTotalTurnsCompleted());
    }

    @Test
    void findByDoctorId_ReturnsEmptyWhenNotExists() {
        User doctor = createTestDoctor();

        Optional<DoctorBadgeStatistics> foundStats = statisticsRepository.findByDoctorId(doctor.getId());

        assertFalse(foundStats.isPresent());
    }

    @Test
    @Transactional
    void incrementRatingCounters_IncrementsAllCounters() {
        User doctor = createTestDoctor();
        createTestStatistics(doctor);

        statisticsRepository.incrementRatingCounters(doctor.getId(), true, true, true);

        entityManager.flush();
        entityManager.clear();

        DoctorBadgeStatistics updatedStats = statisticsRepository.findByDoctorId(doctor.getId()).get();
        assertEquals(51, updatedStats.getTotalRatingsReceived()); 
        assertEquals(11, updatedStats.getLast50CommunicationCount()); 
        assertEquals(13, updatedStats.getLast50EmpathyCount()); 
        assertEquals(9, updatedStats.getLast50PunctualityCount()); 
        assertTrue(updatedStats.getLastUpdatedAt().isAfter(OffsetDateTime.now().minusMinutes(1)));
    }

    @Test
    @Transactional
    void incrementRatingCounters_IncrementsOnlySpecifiedCounters() {
        User doctor = createTestDoctor();
        createTestStatistics(doctor);

        statisticsRepository.incrementRatingCounters(doctor.getId(), true, false, true);

        entityManager.flush();
        entityManager.clear();

        DoctorBadgeStatistics updatedStats = statisticsRepository.findByDoctorId(doctor.getId()).get();
        assertEquals(51, updatedStats.getTotalRatingsReceived()); 
        assertEquals(11, updatedStats.getLast50CommunicationCount()); 
        assertEquals(12, updatedStats.getLast50EmpathyCount()); 
        assertEquals(9, updatedStats.getLast50PunctualityCount()); 
    }

    @Test
    @Transactional
    void incrementTurnCompleted_IncrementsTurnCounters() {
        User doctor = createTestDoctor();
        createTestStatistics(doctor);

        statisticsRepository.incrementTurnCompleted(doctor.getId());

        entityManager.flush();
        entityManager.clear();

        DoctorBadgeStatistics updatedStats = statisticsRepository.findByDoctorId(doctor.getId()).get();
        assertEquals(101, updatedStats.getTotalTurnsCompleted()); 
        assertEquals(21, updatedStats.getTurnsLast90Days()); 
        assertTrue(updatedStats.getLastUpdatedAt().isAfter(OffsetDateTime.now().minusMinutes(1)));
    }

    @Test
    @Transactional
    void incrementTurnCancelled_IncrementsCancellationCounters() {
        User doctor = createTestDoctor();
        createTestStatistics(doctor);

        statisticsRepository.incrementTurnCancelled(doctor.getId());

        entityManager.flush();
        entityManager.clear();

        DoctorBadgeStatistics updatedStats = statisticsRepository.findByDoctorId(doctor.getId()).get();
        assertEquals(6, updatedStats.getTotalTurnsCancelled()); 
        assertEquals(3, updatedStats.getCancellationsLast90Days()); 
        assertTrue(updatedStats.getLastUpdatedAt().isAfter(OffsetDateTime.now().minusMinutes(1)));
    }

    @Test
    @Transactional
    void incrementDocumentation_IncrementsDocumentationCounters() {
        User doctor = createTestDoctor();
        createTestStatistics(doctor);

        statisticsRepository.incrementDocumentation(doctor.getId());

        entityManager.flush();
        entityManager.clear();

        DoctorBadgeStatistics updatedStats = statisticsRepository.findByDoctorId(doctor.getId()).get();
        assertEquals(46, updatedStats.getLast50DocumentedCount()); 
        assertEquals(26, updatedStats.getLast30DocumentedCount()); 
        assertTrue(updatedStats.getLastUpdatedAt().isAfter(OffsetDateTime.now().minusMinutes(1)));
    }

    @Test
    @Transactional
    void incrementRequestHandled_IncrementsWhenBelowLimit() {
        User doctor = createTestDoctor();
        createTestStatistics(doctor);

        statisticsRepository.incrementRequestHandled(doctor.getId());

        entityManager.flush();
        entityManager.clear();

        DoctorBadgeStatistics updatedStats = statisticsRepository.findByDoctorId(doctor.getId()).get();
        assertEquals(6, updatedStats.getLast10RequestsHandled()); 
        assertTrue(updatedStats.getLastUpdatedAt().isAfter(OffsetDateTime.now().minusMinutes(1)));
    }

    @Test
    @Transactional
    void incrementRequestHandled_DoesNotExceedLimit() {
        User doctor = createTestDoctor();
        DoctorBadgeStatistics stats = createTestStatistics(doctor);
        stats.setLast10RequestsHandled(10); 
        entityManager.persistAndFlush(stats);

        statisticsRepository.incrementRequestHandled(doctor.getId());

        entityManager.flush();
        entityManager.clear();

        DoctorBadgeStatistics updatedStats = statisticsRepository.findByDoctorId(doctor.getId()).get();
        assertEquals(10, updatedStats.getLast10RequestsHandled()); 
        assertTrue(updatedStats.getLastUpdatedAt().isAfter(OffsetDateTime.now().minusMinutes(1)));
    }

    @Test
    @Transactional
    void incrementRequestHandled_IncrementsWhenAtLimitMinusOne() {
        User doctor = createTestDoctor();
        DoctorBadgeStatistics stats = createTestStatistics(doctor);
        stats.setLast10RequestsHandled(9); 
        entityManager.persistAndFlush(stats);

        statisticsRepository.incrementRequestHandled(doctor.getId());

        entityManager.flush();
        entityManager.clear();

        DoctorBadgeStatistics updatedStats = statisticsRepository.findByDoctorId(doctor.getId()).get();
        assertEquals(10, updatedStats.getLast10RequestsHandled()); 
        assertTrue(updatedStats.getLastUpdatedAt().isAfter(OffsetDateTime.now().minusMinutes(1)));
    }
}