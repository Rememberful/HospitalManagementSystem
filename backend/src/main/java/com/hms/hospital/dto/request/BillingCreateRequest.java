package com.hms.hospital.dto.request;

import com.hms.hospital.entity.BillingItem;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BillingCreateRequest(
        @NotNull(message = "is required") Long patient,
        Integer stayId,
        @NotEmpty(message = "must include at least one line item") List<BillingItem> items) {
}
