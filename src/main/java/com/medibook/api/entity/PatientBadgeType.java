package com.medibook.api.entity;

public enum PatientBadgeType {
    MEDIBOOK_WELCOME,

    HEALTH_GUARDIAN,
    COMMITTED_PATIENT,
    CONTINUOUS_FOLLOWUP,
    CONSTANT_PATIENT,

    EXEMPLARY_PUNCTUALITY,
    SMART_PLANNER,
    EXCELLENT_COLLABORATOR,

    ALWAYS_PREPARED,
    RESPONSIBLE_EVALUATOR,
    EXCELLENCE_MODEL;

    public PatientBadgeCategory getCategory() {
        return switch (this) {
            case MEDIBOOK_WELCOME -> PatientBadgeCategory.WELCOME;
            case HEALTH_GUARDIAN, COMMITTED_PATIENT,
                 CONTINUOUS_FOLLOWUP, CONSTANT_PATIENT -> PatientBadgeCategory.PREVENTIVE_CARE;
            case EXEMPLARY_PUNCTUALITY, SMART_PLANNER, EXCELLENT_COLLABORATOR -> PatientBadgeCategory.ACTIVE_COMMITMENT;
            case ALWAYS_PREPARED, RESPONSIBLE_EVALUATOR, EXCELLENCE_MODEL -> PatientBadgeCategory.CLINICAL_EXCELLENCE;
        };
    }

    public enum PatientBadgeCategory {
        WELCOME,
        PREVENTIVE_CARE,
        ACTIVE_COMMITMENT,
        CLINICAL_EXCELLENCE
    }
}
