package com.medibook.api.dto.Rating;

import java.util.Arrays;

public enum RatingSubcategoryPatient {
    PUNTUALIDAD("Puntualidad"),
    RESPETO("Respeto"),
    HIGIENE("Higiene");

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
