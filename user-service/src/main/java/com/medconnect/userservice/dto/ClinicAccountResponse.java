package com.medconnect.userservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class ClinicAccountResponse {
    private String id;
    private String name;
    private String siretNumber;
    private String ownerUserId;
    private List<String> teamMemberIds;
}
