package com.cabybara.prolearningplatform.dto.response.todo;

import com.cabybara.prolearningplatform.enums.TodoPriority;
import com.cabybara.prolearningplatform.enums.TodoStatus;
import com.cabybara.prolearningplatform.enums.TodoType;
import com.cabybara.prolearningplatform.model.ResourceRef;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

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
    private TodoType type;
    private TodoStatus status;
    private List<ResourceRef> setRefs;
    private List<ResourceRef> noteRefs;
    private List<ResourceRef> flashcardRefs;
    private List<ResourceRef> examRefs;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Boolean calendarSynced;
}
