package com.medibook.api.service;
import com.medibook.api.dto.Badge.BadgeProgressSummaryDTO;
import com.medibook.api.entity.BadgeType;
import com.medibook.api.entity.BadgeType.BadgeCategory;
import com.medibook.api.entity.DoctorBadgeStatistics;
import com.medibook.api.entity.User;
import com.medibook.api.repository.DoctorBadgeRepository;
import com.medibook.api.repository.DoctorBadgeStatisticsRepository;
import com.medibook.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BadgeProgressService {

    private final DoctorBadgeStatisticsRepository statisticsRepository;
    private final UserRepository userRepository;
    private final DoctorBadgeRepository badgeRepository;

    @Transactional(readOnly = true)
    public List<BadgeProgressSummaryDTO> getBadgeProgress(UUID doctorId) {
        log.info("Fetching badge progress for doctor: {}", doctorId);

        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        if (!"DOCTOR".equals(doctor.getRole())) {
            throw new RuntimeException("User is not a doctor");
        }

        DoctorBadgeStatistics stats = statisticsRepository.findByDoctorId(doctorId)
                .orElseGet(() -> createEmptyStatistics(doctorId));

        List<BadgeType> earnedBadges = badgeRepository.findByDoctor_IdAndIsActiveTrue(doctorId)
                .stream()
                .map(badge -> badge.getBadgeType())
                .toList();
        
        List<BadgeProgressSummaryDTO> progressList = new ArrayList<>();

        progressList.add(createProgressDTO(
                BadgeType.SUSTAINED_EXCELLENCE,
                "Excelencia Sostenida",
                BadgeCategory.QUALITY_OF_CARE,
                stats.getProgressExcellenceInCare(),
                earnedBadges.contains(BadgeType.SUSTAINED_EXCELLENCE),
                "Mantén un promedio de 4.7⭐ en los últimos 100 turnos"
        ));

        progressList.add(createProgressDTO(
                BadgeType.EMPATHETIC_DOCTOR,
                "Médico Empático",
                BadgeCategory.QUALITY_OF_CARE,
                stats.getProgressEmpathyChampion(),
                earnedBadges.contains(BadgeType.EMPATHETIC_DOCTOR),
                "Recibe 15+ menciones de empatía en los últimos 50 comentarios"
        ));

        progressList.add(createProgressDTO(
                BadgeType.EXCEPTIONAL_COMMUNICATOR,
                "Comunicador Excepcional",
                BadgeCategory.QUALITY_OF_CARE,
                stats.getProgressClearCommunicator(),
                earnedBadges.contains(BadgeType.EXCEPTIONAL_COMMUNICATOR),
                "Recibe 15+ menciones de comunicación clara en los últimos 50 comentarios"
        ));

        progressList.add(createProgressDTO(
                BadgeType.DETAILED_HISTORIAN,
                "Historiador Detallado",
                BadgeCategory.QUALITY_OF_CARE,
                stats.getProgressDetailedDiagnostician(),
                earnedBadges.contains(BadgeType.DETAILED_HISTORIAN),
                "Promedio de 150+ palabras en las últimas 30 historias médicas"
        ));

        progressList.add(createProgressDTO(
                BadgeType.PUNCTUALITY_PROFESSIONAL,
                "Puntualidad Profesional",
                BadgeCategory.PROFESSIONALISM,
                stats.getProgressTimelyProfessional(),
                earnedBadges.contains(BadgeType.PUNCTUALITY_PROFESSIONAL),
                "Recibe 12+ menciones de puntualidad en los últimos 50 comentarios"
        ));

        progressList.add(createProgressDTO(
                BadgeType.COMPLETE_DOCUMENTER,
                "Documentador Completo",
                BadgeCategory.PROFESSIONALISM,
                stats.getProgressReliableExpert(),
                earnedBadges.contains(BadgeType.COMPLETE_DOCUMENTER),
                "96% de documentación en los últimos 50 turnos completados"
        ));

        progressList.add(createProgressDTO(
                BadgeType.CONSISTENT_PROFESSIONAL,
                "Profesional Consistente",
                BadgeCategory.CONSISTENCY,
                stats.getProgressFlexibleCaregiver(),
                earnedBadges.contains(BadgeType.CONSISTENT_PROFESSIONAL),
                "Mantén una tasa de cancelación <10% con 20+ turnos"
        ));

        progressList.add(createProgressDTO(
                BadgeType.AGILE_RESPONDER,
                "Respuesta Ágil",
                BadgeCategory.PROFESSIONALISM,
                stats.getProgressAgileResponder(),
                earnedBadges.contains(BadgeType.AGILE_RESPONDER),
                "Responde a 90% de solicitudes en <24 horas (últimas 10)"
        ));

        progressList.add(createProgressDTO(
                BadgeType.RELATIONSHIP_BUILDER,
                "Constructor de Relaciones",
                BadgeCategory.PROFESSIONALISM,
                stats.getProgressRelationshipBuilder(),
                earnedBadges.contains(BadgeType.RELATIONSHIP_BUILDER),
                "Consigue 10+ pacientes que regresan de un total de 50+ únicos"
        ));

        progressList.add(createProgressDTO(
                BadgeType.TOP_SPECIALIST,
                "Especialista TOP",
                BadgeCategory.CONSISTENCY,
                stats.getProgressTopSpecialist(),
                earnedBadges.contains(BadgeType.TOP_SPECIALIST),
                "Ubícate en el top 10% de tu especialidad con 100+ turnos"
        ));

        progressList.add(createProgressDTO(
                BadgeType.MEDICAL_LEGEND,
                "Leyenda Médica",
                BadgeCategory.CONSISTENCY,
                stats.getProgressMedicalLegend(),
                earnedBadges.contains(BadgeType.MEDICAL_LEGEND),
                "Completa 500+ turnos con promedio de 4.7⭐ y 8+ badges"
        ));

        progressList.add(createProgressDTO(
                BadgeType.ALWAYS_AVAILABLE,
                "Siempre Disponible",
                BadgeCategory.CONSISTENCY,
                stats.getProgressAllStarDoctor(),
                earnedBadges.contains(BadgeType.ALWAYS_AVAILABLE),
                "Disponibilidad en 4+ días/semana por 60+ días consecutivos"
        ));

        log.info("Badge progress fetched successfully for doctor: {}", doctorId);
        return progressList;
    }

    private BadgeProgressSummaryDTO createProgressDTO(
            BadgeType type,
            String name,
            BadgeCategory category,
            Double progress,
            Boolean earned,
            String description
    ) {
        String statusMessage = earned ?
                "¡Insignia obtenida! Excelente trabajo." :
                (progress >= 75 ? "¡Casi lo logras! Sigue así." :
                        progress >= 50 ? "¡Buen progreso! Estás a mitad de camino." :
                                progress >= 25 ? "¡Vas bien! Continúa esforzándote." :
                                        "Comienza a trabajar en esta insignia.");

        return BadgeProgressSummaryDTO.builder()
                .badgeType(type)
                .badgeName(name)
                .category(category)
                .earned(earned)
                .progressPercentage(progress)
                .description(description)
                .statusMessage(statusMessage)
                .build();
    }

    private DoctorBadgeStatistics createEmptyStatistics(UUID doctorId) {
        return DoctorBadgeStatistics.builder()
                .doctorId(doctorId)
                .totalRatingsReceived(0)
                .last50CommunicationCount(0)
                .last50EmpathyCount(0)
                .last50PunctualityCount(0)
                .totalTurnsCompleted(0)
                .totalTurnsCancelled(0)
                .last30DocumentedCount(0)
                .totalUniquePatients(0)
                .returningPatientsCount(0)
                .last10RequestsHandled(0)
                .progressExcellenceInCare(0.0)
                .progressEmpathyChampion(0.0)
                .progressClearCommunicator(0.0)
                .progressDetailedDiagnostician(0.0)
                .progressTimelyProfessional(0.0)
                .progressReliableExpert(0.0)
                .progressFlexibleCaregiver(0.0)
                .progressAgileResponder(0.0)
                .progressRelationshipBuilder(0.0)
                .progressTopSpecialist(0.0)
                .progressMedicalLegend(0.0)
                .progressAllStarDoctor(0.0)
                .build();
    }
}
