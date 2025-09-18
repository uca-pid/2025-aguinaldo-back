package com.medibook.api.dto.Admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorApprovalResponseDTO {
    private String message;
    private String doctorId;
    private String newStatus;
}