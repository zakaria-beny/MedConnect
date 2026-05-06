package com.medconnect.userservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class BulkImportResponse {
    private String id;
    private String userId;
    private String fileName;
    private String status;
    private int totalRows;
    private int successCount;
    private int failedCount;
    private List<String> errors;
}
