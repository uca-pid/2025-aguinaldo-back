package com.medibook.api.repository;

import com.medibook.api.entity.BadgeType;
import com.medibook.api.entity.DoctorBadge;
import com.medibook.api.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class DoctorBadgeRepositoryTest {

    @Autowired
    private DoctorBadgeRepository doctorBadgeRepository;

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

    private DoctorBadge createTestBadge(User doctor, BadgeType badgeType, boolean isActive) {
        DoctorBadge badge = DoctorBadge.builder()
                .doctor(doctor)
                .badgeType(badgeType)
                .earnedAt(OffsetDateTime.now().minusDays(1))
                .isActive(isActive)
                .lastEvaluatedAt(OffsetDateTime.now())
                .build();
        return entityManager.persistAndFlush(badge);
    }

    @Test
    void findByDoctor_IdAndIsActiveTrue_ReturnsOnlyActiveBadges() {
        User doctor = createTestDoctor();

        createTestBadge(doctor, BadgeType.EXCEPTIONAL_COMMUNICATOR, true);
        createTestBadge(doctor, BadgeType.EMPATHETIC_DOCTOR, false);
        createTestBadge(doctor, BadgeType.PUNCTUALITY_PROFESSIONAL, true);

        List<DoctorBadge> activeBadges = doctorBadgeRepository.findByDoctor_IdAndIsActiveTrue(doctor.getId());

        assertEquals(2, activeBadges.size());
        assertTrue(activeBadges.stream().allMatch(DoctorBadge::getIsActive));
        assertTrue(activeBadges.stream().anyMatch(b -> b.getBadgeType() == BadgeType.EXCEPTIONAL_COMMUNICATOR));
        assertTrue(activeBadges.stream().anyMatch(b -> b.getBadgeType() == BadgeType.PUNCTUALITY_PROFESSIONAL));
        assertFalse(activeBadges.stream().anyMatch(b -> b.getBadgeType() == BadgeType.EMPATHETIC_DOCTOR));
    }

    @Test
    void findByDoctor_IdAndIsActiveTrue_ReturnsEmptyListWhenNoActiveBadges() {
        User doctor = createTestDoctor();
        createTestBadge(doctor, BadgeType.EXCEPTIONAL_COMMUNICATOR, false);

        List<DoctorBadge> activeBadges = doctorBadgeRepository.findByDoctor_IdAndIsActiveTrue(doctor.getId());

        assertTrue(activeBadges.isEmpty());
    }

    @Test
    void findByDoctor_IdOrderByEarnedAtDesc_ReturnsBadgesOrderedByEarnedDate() {
        User doctor = createTestDoctor();

        DoctorBadge badge1 = createTestBadge(doctor, BadgeType.EXCEPTIONAL_COMMUNICATOR, true);
        badge1.setEarnedAt(OffsetDateTime.now().minusDays(3));
        entityManager.persistAndFlush(badge1);

        DoctorBadge badge2 = createTestBadge(doctor, BadgeType.EMPATHETIC_DOCTOR, true);
        badge2.setEarnedAt(OffsetDateTime.now().minusDays(1));
        entityManager.persistAndFlush(badge2);

        DoctorBadge badge3 = createTestBadge(doctor, BadgeType.PUNCTUALITY_PROFESSIONAL, true);
        badge3.setEarnedAt(OffsetDateTime.now().minusDays(2));
        entityManager.persistAndFlush(badge3);

        List<DoctorBadge> badges = doctorBadgeRepository.findByDoctor_IdOrderByEarnedAtDesc(doctor.getId());

        assertEquals(3, badges.size());
        assertEquals(BadgeType.EMPATHETIC_DOCTOR, badges.get(0).getBadgeType()); 
        assertEquals(BadgeType.PUNCTUALITY_PROFESSIONAL, badges.get(1).getBadgeType()); 
        assertEquals(BadgeType.EXCEPTIONAL_COMMUNICATOR, badges.get(2).getBadgeType()); 
    }

    @Test
    void findByDoctor_IdAndBadgeType_ReturnsSpecificBadge() {
        User doctor = createTestDoctor();
        DoctorBadge badge = createTestBadge(doctor, BadgeType.EXCEPTIONAL_COMMUNICATOR, true);

        Optional<DoctorBadge> foundBadge = doctorBadgeRepository.findByDoctor_IdAndBadgeType(doctor.getId(), BadgeType.EXCEPTIONAL_COMMUNICATOR);

        assertTrue(foundBadge.isPresent());
        assertEquals(badge.getId(), foundBadge.get().getId());
        assertEquals(BadgeType.EXCEPTIONAL_COMMUNICATOR, foundBadge.get().getBadgeType());
    }

    @Test
    void findByDoctor_IdAndBadgeType_ReturnsEmptyWhenBadgeNotFound() {
        User doctor = createTestDoctor();
        createTestBadge(doctor, BadgeType.EXCEPTIONAL_COMMUNICATOR, true);

        Optional<DoctorBadge> foundBadge = doctorBadgeRepository.findByDoctor_IdAndBadgeType(doctor.getId(), BadgeType.EMPATHETIC_DOCTOR);

        assertFalse(foundBadge.isPresent());
    }

    @Test
    void existsByDoctor_IdAndBadgeType_ReturnsTrueWhenBadgeExists() {
        User doctor = createTestDoctor();
        createTestBadge(doctor, BadgeType.EXCEPTIONAL_COMMUNICATOR, true);

        boolean exists = doctorBadgeRepository.existsByDoctor_IdAndBadgeType(doctor.getId(), BadgeType.EXCEPTIONAL_COMMUNICATOR);

        assertTrue(exists);
    }

    @Test
    void existsByDoctor_IdAndBadgeType_ReturnsFalseWhenBadgeDoesNotExist() {
        User doctor = createTestDoctor();

        boolean exists = doctorBadgeRepository.existsByDoctor_IdAndBadgeType(doctor.getId(), BadgeType.EXCEPTIONAL_COMMUNICATOR);

        assertFalse(exists);
    }

    @Test
    void existsByDoctor_IdAndBadgeTypeAndIsActive_ReturnsTrueWhenActiveBadgeExists() {
        User doctor = createTestDoctor();
        createTestBadge(doctor, BadgeType.EXCEPTIONAL_COMMUNICATOR, true);

        boolean exists = doctorBadgeRepository.existsByDoctor_IdAndBadgeTypeAndIsActive(doctor.getId(), BadgeType.EXCEPTIONAL_COMMUNICATOR, true);

        assertTrue(exists);
    }

    @Test
    void existsByDoctor_IdAndBadgeTypeAndIsActive_ReturnsFalseWhenBadgeInactive() {
        User doctor = createTestDoctor();
        createTestBadge(doctor, BadgeType.EXCEPTIONAL_COMMUNICATOR, false);

        boolean exists = doctorBadgeRepository.existsByDoctor_IdAndBadgeTypeAndIsActive(doctor.getId(), BadgeType.EXCEPTIONAL_COMMUNICATOR, true);

        assertFalse(exists);
    }

    @Test
    void countActiveBadgesByDoctorId_ReturnsCorrectCount() {
        User doctor = createTestDoctor();

        createTestBadge(doctor, BadgeType.EXCEPTIONAL_COMMUNICATOR, true);
        createTestBadge(doctor, BadgeType.EMPATHETIC_DOCTOR, true);
        createTestBadge(doctor, BadgeType.PUNCTUALITY_PROFESSIONAL, false);

        long count = doctorBadgeRepository.countActiveBadgesByDoctorId(doctor.getId());

        assertEquals(2, count);
    }

    @Test
    void countActiveBadgesByDoctorId_ReturnsZeroWhenNoActiveBadges() {
        User doctor = createTestDoctor();
        createTestBadge(doctor, BadgeType.EXCEPTIONAL_COMMUNICATOR, false);

        long count = doctorBadgeRepository.countActiveBadgesByDoctorId(doctor.getId());

        assertEquals(0, count);
    }

    @Test
    void countActiveBadgesByDoctorIdExcludingType_ExcludesSpecifiedType() {
        User doctor = createTestDoctor();

        createTestBadge(doctor, BadgeType.EXCEPTIONAL_COMMUNICATOR, true);
        createTestBadge(doctor, BadgeType.EMPATHETIC_DOCTOR, true);
        createTestBadge(doctor, BadgeType.PUNCTUALITY_PROFESSIONAL, true);

        long count = doctorBadgeRepository.countActiveBadgesByDoctorIdExcludingType(doctor.getId(), BadgeType.EXCEPTIONAL_COMMUNICATOR);

        assertEquals(2, count);
    }

    @Test
    void countActiveBadgesByDoctorIdExcludingType_IncludesExcludedTypeWhenInactive() {
        User doctor = createTestDoctor();

        createTestBadge(doctor, BadgeType.EXCEPTIONAL_COMMUNICATOR, false); // Inactive
        createTestBadge(doctor, BadgeType.EMPATHETIC_DOCTOR, true);
        createTestBadge(doctor, BadgeType.PUNCTUALITY_PROFESSIONAL, true);

        long count = doctorBadgeRepository.countActiveBadgesByDoctorIdExcludingType(doctor.getId(), BadgeType.EXCEPTIONAL_COMMUNICATOR);

        assertEquals(2, count); // Should still count the other two active badges
    }

    @Test
    void findByBadgeTypeAndIsActiveTrue_ReturnsAllActiveBadgesOfType() {
        User doctor1 = createTestDoctor();
        User doctor2 = createTestDoctor();
        doctor2.setEmail("doctor" + (dniCounter - 1) + "@test.com");
        entityManager.persistAndFlush(doctor2);

        createTestBadge(doctor1, BadgeType.EXCEPTIONAL_COMMUNICATOR, true);
        createTestBadge(doctor2, BadgeType.EXCEPTIONAL_COMMUNICATOR, true);
        createTestBadge(doctor1, BadgeType.EMPATHETIC_DOCTOR, false); // Inactive, different type

        List<DoctorBadge> badges = doctorBadgeRepository.findByBadgeTypeAndIsActiveTrue(BadgeType.EXCEPTIONAL_COMMUNICATOR);

        assertEquals(2, badges.size());
        assertTrue(badges.stream().allMatch(b -> b.getBadgeType() == BadgeType.EXCEPTIONAL_COMMUNICATOR));
        assertTrue(badges.stream().allMatch(DoctorBadge::getIsActive));
    }

    @Test
    void findByBadgeTypeAndIsActiveTrue_ReturnsEmptyListWhenNoActiveBadgesOfType() {
        createTestBadge(createTestDoctor(), BadgeType.EXCEPTIONAL_COMMUNICATOR, false);

        List<DoctorBadge> badges = doctorBadgeRepository.findByBadgeTypeAndIsActiveTrue(BadgeType.EXCEPTIONAL_COMMUNICATOR);

        assertTrue(badges.isEmpty());
    }
}