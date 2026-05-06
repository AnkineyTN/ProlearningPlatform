package com.cabybara.prolearningplatform.dto.response.share;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class YjsStateResponse {
    private String yjsState; // base64, null nếu note mới
}
