package com.cabybara.prolearningplatform.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageResponseDetail<T> {
    private int pageNo;
    private int pageSize;
    private int totalPage;
    private Long totalElements;
    private T items;
}