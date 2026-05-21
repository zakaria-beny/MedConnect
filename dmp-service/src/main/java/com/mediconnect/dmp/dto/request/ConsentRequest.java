package com.mediconnect.dmp.dto.request;

import com.mediconnect.dmp.model.Consent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsentRequest {

    @NotBlank(message = "Doctor ID is required")
    private String doctorId;

    @NotBlank(message = "Doctor name is required")
    private String doctorName;

    private List<String> allowedSections;

    @NotNull(message = "Access level is required")
    private Consent.AccessLevel accessLevel;

    private LocalDateTime expiresAt;
}
