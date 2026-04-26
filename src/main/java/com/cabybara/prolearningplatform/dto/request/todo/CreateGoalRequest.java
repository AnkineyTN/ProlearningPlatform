package com.cabybara.prolearningplatform.dto.request.todo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateGoalRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String description;

    private LocalDate targetDate;

    @Size(max = 10, message = "Color must not exceed 10 characters")
    private String color;
}
