package com.medconnect.userservice.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document("clinic_accounts")
public class ClinicAccount {
    @Id
    private String id;

    @Indexed(unique = true)
    private String siretNumber;

    @Indexed
    private String ownerUserId;

    private String name;
    private List<String> teamMemberIds = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
