package com.medibook.api.dto.Rating;

import java.util.Arrays;

public enum RatingSubcategoryPatient {
    // Positive
    LLEGA_PUNTUAL("Llega puntual"),
    RESPETUOSO("Respetuoso"),
    BUENA_HIGIENE("Buena higiene"),
    COLABORA_EN_CONSULTA("Colabora en consulta"),
    SE_COMUNICA_BIEN("Se comunica bien"),
    SIGUE_INDICACIONES("Sigue indicaciones"),
    PACIENTE_COMPROMETIDO("Paciente comprometido"),
    RESPONSABLE("Responsable"),
    
    // Neutral/Mixed
    COMUNICACION_REGULAR("Comunicación regular"),
    ASISTE_A_CITAS("Asiste a citas"),
    
    // Negative
    LLEGA_TARDE("Llega tarde"),
    FALTA_DE_RESPETO("Falta de respeto"),
    MALA_HIGIENE("Mala higiene"),
    NO_COLABORA("No colabora"),
    NO_SIGUE_INDICACIONES("No sigue indicaciones"),
    CANCELA_FRECUENTEMENTE("Cancela frecuentemente"),
    NO_ASISTE("No asiste"),
    AGRESIVO("Agresivo");

    private final String label;

    RatingSubcategoryPatient(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static RatingSubcategoryPatient fromString(String s) {
        if (s == null) return null;
        String trimmed = s.trim();
        for (RatingSubcategoryPatient r : values()) {
            if (r.label.equalsIgnoreCase(trimmed)) return r;
        }
        try {
            return RatingSubcategoryPatient.valueOf(trimmed.toUpperCase().replaceAll("[^A-Z0-9]","_"));
        } catch (Exception e) {
            return null;
        }
    }

    public static String allowedValues() {
        return Arrays.stream(values()).map(RatingSubcategoryPatient::getLabel).reduce((a,b) -> a+", "+b).orElse("");
    }
}
