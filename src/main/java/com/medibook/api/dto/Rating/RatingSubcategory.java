package com.medibook.api.dto.Rating;

import java.util.Arrays;

public enum RatingSubcategory {
    // Positive
    EXCELENTE_ATENCION("Excelente atención"),
    EXPLICA_CLARAMENTE("Explica claramente"),
    DEMUESTRA_EMPATIA("Demuestra empatía"),
    RESPETA_HORARIOS("Respeta horarios"),
    CONSULTORIO_LIMPIO("Consultorio limpio"),
    PROFESIONAL_COMPETENTE("Profesional competente"),
    ESCUCHA_AL_PACIENTE("Escucha al paciente"),
    GENERA_CONFIANZA("Genera confianza"),
    
    // Neutral/Mixed
    TIEMPO_DE_ESPERA_ACEPTABLE("Tiempo de espera aceptable"),
    EXAMINA_ADECUADAMENTE("Examina adecuadamente"),
    
    // Negative
    NO_EXPLICA_BIEN("No explica bien"),
    ATENCION_APRESURADA("Atención apresurada"),
    FALTA_DE_EMPATIA("Falta de empatía"),
    NO_RESPETA_HORARIOS("No respeta horarios"),
    CONSULTORIO_SUCIO("Consultorio sucio"),
    NO_ESCUCHA("No escucha"),
    GENERA_DESCONFIANZA("Genera desconfianza"),
    DIAGNOSTICO_DUDOSO("Diagnóstico dudoso");

    private final String label;

    RatingSubcategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static RatingSubcategory fromString(String s) {
        if (s == null) return null;
        String trimmed = s.trim();
        for (RatingSubcategory r : values()) {
            if (r.label.equalsIgnoreCase(trimmed)) return r;
        }
        try {
            return RatingSubcategory.valueOf(trimmed.toUpperCase().replaceAll("[^A-Z0-9]","_"));
        } catch (Exception e) {
            return null;
        }
    }

    public static String allowedValues() {
        return Arrays.stream(values()).map(RatingSubcategory::getLabel).reduce((a,b) -> a+", "+b).orElse("");
    }
}
