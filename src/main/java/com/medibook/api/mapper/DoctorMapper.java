package com.medibook.api.mapper;
import com.medibook.api.dto.Badge.BadgeDTO;
import com.medibook.api.dto.DoctorDTO;
import com.medibook.api.entity.DoctorBadge;
import com.medibook.api.entity.User;
import com.medibook.api.repository.DoctorBadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DoctorMapper {

    private final DoctorBadgeRepository badgeRepository;

    public DoctorDTO toDTO(User user) {
        if (user == null || user.getDoctorProfile() == null) {
            return null;
        }

        List<DoctorBadge> activeBadges = badgeRepository.findByDoctor_IdAndIsActiveTrue(user.getId());
        
        List<BadgeDTO> badgeDTOs = activeBadges.stream()
                .map(badge -> BadgeDTO.builder()
                        .badgeType(badge.getBadgeType())
                        .category(badge.getBadgeType().getCategory().name())
                        .isActive(badge.getIsActive())
                        .earnedAt(badge.getEarnedAt())
                        .lastEvaluatedAt(badge.getLastEvaluatedAt())
                        .build())
                .collect(Collectors.toList());

        return DoctorDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .email(user.getEmail())
                .medicalLicense(user.getDoctorProfile().getMedicalLicense())
                .specialty(user.getDoctorProfile().getSpecialty())
                .slotDurationMin(user.getDoctorProfile().getSlotDurationMin())
                .score(user.getScore())
                .activeBadges(badgeDTOs)
                .totalActiveBadges(badgeDTOs.size())
                .build();
    }
}