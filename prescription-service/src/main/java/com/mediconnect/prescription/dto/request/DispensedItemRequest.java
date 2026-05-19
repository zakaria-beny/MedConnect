package com.mediconnect.prescription.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispensedItemRequest {
    private String drugName;
    private int quantityDispensed;
    private String batchNumber;
}
