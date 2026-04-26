package com.cabybara.prolearningplatform.dto.request.todo;

import com.cabybara.prolearningplatform.enums.TodoPriority;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateTodoRequest {

    @Size(max = 500, message = "Title must not exceed 500 characters")
    private String title;

    private String description;

    private TodoPriority priority;

    private LocalDate dueDate;

    private Boolean completed;

    private Long goalId;

    private boolean clearGoal;
}
