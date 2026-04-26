package com.cabybara.prolearningplatform.dto.response.todo;

import com.cabybara.prolearningplatform.enums.GoalStatus;
import com.cabybara.prolearningplatform.enums.GoalType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
public class GoalWithTodosResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDate targetDate;
    private String color;
    private GoalStatus status;
    private GoalType type;
    private Long parentGoalId;
    private long totalTodos;
    private long completedTodos;
    private int progress;
    private List<TodoResponse> todos;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
