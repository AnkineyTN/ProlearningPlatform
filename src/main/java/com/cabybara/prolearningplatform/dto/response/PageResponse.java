package com.cabybara.prolearningplatform.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PageResponse<T> {
    private int totalElements;
    private T items;
}