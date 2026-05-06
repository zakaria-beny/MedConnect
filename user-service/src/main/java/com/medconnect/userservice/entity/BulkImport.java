package com.medconnect.userservice.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document("bulk_imports")
public class BulkImport {
    @Id
    private String id;

    @Indexed
    private String userId;

    private String fileName;
    private BulkImportStatus status;
    private int totalRows;
    private int successCount;
    private int failedCount;
    private List<String> errors = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
