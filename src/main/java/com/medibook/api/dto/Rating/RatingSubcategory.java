package com.medibook.api.dto.Rating;

import java.util.Arrays;

public enum RatingSubcategory {
    ATIENDEN_EN_HORARIO("Atienden en horario"),
    EXPLICACION_CLARA("Explicación clara"),
    LIMPIEZA_DEL_CONSULTORIO("Limpieza del consultorio");

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
