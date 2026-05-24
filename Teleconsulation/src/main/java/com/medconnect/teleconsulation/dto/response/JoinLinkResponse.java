package com.medconnect.teleconsulation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinLinkResponse {
    private String sessionId;
    private String joinLink;
    private String role;
    private LocalDateTime expiresAt;
}
