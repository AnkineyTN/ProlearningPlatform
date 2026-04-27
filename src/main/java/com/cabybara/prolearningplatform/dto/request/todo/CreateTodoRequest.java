package com.cabybara.prolearningplatform.dto.request.todo;

import com.cabybara.prolearningplatform.enums.TodoPriority;
import com.cabybara.prolearningplatform.enums.TodoStatus;
import com.cabybara.prolearningplatform.enums.TodoType;
import com.cabybara.prolearningplatform.model.ResourceRef;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class CreateTodoRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title must not exceed 500 characters")
    private String title;

    private String description;

    private TodoPriority priority;

    private LocalDate dueDate;

    private Long goalId;

    private TodoType type;

    private TodoStatus status;

    private List<ResourceRef> setRefs = new ArrayList<>();

    private List<ResourceRef> noteRefs = new ArrayList<>();

    private List<ResourceRef> flashcardRefs = new ArrayList<>();

    private List<ResourceRef> examRefs = new ArrayList<>();
}
