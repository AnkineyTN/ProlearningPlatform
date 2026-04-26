package com.cabybara.prolearningplatform.dto.response.todo;

import com.cabybara.prolearningplatform.enums.GoalStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Builder
public class GoalResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDate targetDate;
    private String color;
    private GoalStatus status;
    private long totalTodos;
    private long completedTodos;
    private int progress;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
