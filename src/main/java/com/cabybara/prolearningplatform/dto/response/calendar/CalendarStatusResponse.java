package com.cabybara.prolearningplatform.dto.response.calendar;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CalendarStatusResponse {
    private boolean connected;
    private boolean syncEnabled;
}
