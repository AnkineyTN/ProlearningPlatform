package com.cabybara.prolearningplatform.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Data
@Builder
public class PaginationResponseDto {
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private int pageSize;
}
