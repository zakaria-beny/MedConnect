package com.medconnect.userservice.security.payload;

import lombok.Data;
import java.util.List;

@Data
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String id;
    private String email;
    private List<String> roles;
    private String sessionId;
    private String refreshToken;

    public JwtResponse(String accessToken, String id, String email, List<String> roles) {
        this(accessToken, id, email, roles, null);
    }

    public JwtResponse(String accessToken, String id, String email, List<String> roles, String sessionId) {
        this(accessToken, id, email, roles, sessionId, null);
    }

    public JwtResponse(String accessToken, String id, String email, List<String> roles, String sessionId, String refreshToken) {
        this.token = accessToken;
        this.id = id;
        this.email = email;
        this.roles = roles;
        this.sessionId = sessionId;
        this.refreshToken = refreshToken;
    }
}
