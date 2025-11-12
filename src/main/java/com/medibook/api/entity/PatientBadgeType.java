package com.medibook.api.entity;

public enum PatientBadgeType {
    PREVENTIVE_PATIENT,
    TOTAL_COMMITMENT,
    THERAPEUTIC_CONTINUITY,
    CONSTANT_USER,
    ALWAYS_PUNCTUAL,
    EXPERT_PLANNER,
    MODEL_COLLABORATOR,
    PREPARED_PATIENT,
    CONSTRUCTIVE_EVALUATOR,
    EXEMPLARY_PATIENT;

    public PatientBadgeCategory getCategory() {
        return switch (this) {
            case PREVENTIVE_PATIENT, TOTAL_COMMITMENT,
                 THERAPEUTIC_CONTINUITY, CONSTANT_USER -> PatientBadgeCategory.HEALTH_COMMITMENT;
            case ALWAYS_PUNCTUAL, EXPERT_PLANNER, MODEL_COLLABORATOR -> PatientBadgeCategory.RESPONSIBILITY;
            case PREPARED_PATIENT, CONSTRUCTIVE_EVALUATOR, EXEMPLARY_PATIENT -> PatientBadgeCategory.PREPARATION;
        };
    }

    public enum PatientBadgeCategory {
        HEALTH_COMMITMENT,
        RESPONSIBILITY,
        PREPARATION
    }
}
