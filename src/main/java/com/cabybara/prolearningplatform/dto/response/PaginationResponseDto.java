package com.cabybara.prolearningplatform.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaginationResponseDto {
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private int pageSize;
}
