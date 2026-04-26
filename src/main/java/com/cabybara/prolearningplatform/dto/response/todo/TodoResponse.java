package com.cabybara.prolearningplatform.dto.response.todo;

import com.cabybara.prolearningplatform.enums.TodoPriority;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Builder
public class TodoResponse {
    private Long id;
    private String title;
    private String description;
    private Boolean completed;
    private TodoPriority priority;
    private LocalDate dueDate;
    private OffsetDateTime completedAt;
    private Long goalId;
    private String goalTitle;
    private String goalColor;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
