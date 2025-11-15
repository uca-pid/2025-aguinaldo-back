package com.medibook.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medibook.api.dto.Availability.*;
import com.medibook.api.entity.DoctorProfile;
import com.medibook.api.entity.User;
import com.medibook.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorAvailabilityService {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final BadgeEvaluationTriggerService badgeEvaluationTriggerService;

    @Transactional
    public void saveAvailability(UUID doctorId, DoctorAvailabilityRequestDTO request) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        if (doctor.getDoctorProfile() == null) {
            throw new RuntimeException("Doctor profile not found");
        }

        DoctorProfile profile = doctor.getDoctorProfile();
        
        
        try {
            String scheduleJson = objectMapper.writeValueAsString(request.getWeeklyAvailability());
            profile.setAvailabilitySchedule(scheduleJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing availability schedule", e);
        }

        userRepository.save(doctor);
        
        badgeEvaluationTriggerService.evaluateAfterAvailabilityConfigured(doctorId);
    }

    @Transactional(readOnly = true)
    public DoctorAvailabilityResponseDTO getAvailability(UUID doctorId) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        if (doctor.getDoctorProfile() == null) {
            throw new RuntimeException("Doctor profile not found");
        }

        DoctorProfile profile = doctor.getDoctorProfile();
        
        DoctorAvailabilityResponseDTO response = new DoctorAvailabilityResponseDTO();
        response.setSlotDurationMin(profile.getSlotDurationMin());

        
        if (profile.getAvailabilitySchedule() != null) {
            try {
                List<DayAvailabilityDTO> weeklyAvailability = objectMapper.readValue(
                        profile.getAvailabilitySchedule(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, DayAvailabilityDTO.class)
                );
                response.setWeeklyAvailability(weeklyAvailability);
            } catch (JsonProcessingException e) {
                log.error("Error deserializing availability schedule for doctor: {}", doctorId, e);
                response.setWeeklyAvailability(new ArrayList<>());
            }
        } else {
            response.setWeeklyAvailability(new ArrayList<>());
        }

        return response;
    }

    @Transactional(readOnly = true)
    public List<AvailableSlotDTO> getAvailableSlots(UUID doctorId, LocalDate fromDate, LocalDate toDate) {
        DoctorAvailabilityResponseDTO availability = getAvailability(doctorId);
        
        List<AvailableSlotDTO> slots = new ArrayList<>();

        if (availability.getWeeklyAvailability() == null || availability.getWeeklyAvailability().isEmpty()) {
            return slots; 
        }

        
        for (LocalDate currentDate = fromDate; !currentDate.isAfter(toDate); currentDate = currentDate.plusDays(1)) {
            final LocalDate finalCurrentDate = currentDate;
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
            String dayName = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase();

            
            availability.getWeeklyAvailability().stream()
                    .filter(day -> day.getDay().equals(dayName) && day.getEnabled())
                    .forEach(day -> {
                        if (day.getRanges() != null) {
                            day.getRanges().forEach(range -> {
                                List<AvailableSlotDTO> dailySlots = generateSlotsForRange(
                                        finalCurrentDate, range, availability.getSlotDurationMin()
                                );
                                slots.addAll(dailySlots);
                            });
                        }
                    });
        }

        return slots;
    }

    private List<AvailableSlotDTO> generateSlotsForRange(LocalDate date, TimeRangeDTO range, int slotDurationMin) {
        List<AvailableSlotDTO> slots = new ArrayList<>();
        
        LocalTime startTime = LocalTime.parse(range.getStart());
        LocalTime endTime = LocalTime.parse(range.getEnd());
        
        LocalTime currentSlotStart = startTime;
        while (currentSlotStart.plusMinutes(slotDurationMin).isBefore(endTime) || 
               currentSlotStart.plusMinutes(slotDurationMin).equals(endTime)) {
            
            LocalTime currentSlotEnd = currentSlotStart.plusMinutes(slotDurationMin);
            
            AvailableSlotDTO slot = new AvailableSlotDTO();
            slot.setDate(date);
            slot.setStartTime(currentSlotStart);
            slot.setEndTime(currentSlotEnd);
            slot.setDayOfWeek(date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
            
            slots.add(slot);
            currentSlotStart = currentSlotEnd;
        }
        
        return slots;
    }
}