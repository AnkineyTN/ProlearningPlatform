package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.todo.CreateTodoRequest;
import com.cabybara.prolearningplatform.dto.request.todo.UpdateTodoRequest;
import com.cabybara.prolearningplatform.dto.response.PaginationResponseDto;
import com.cabybara.prolearningplatform.dto.response.todo.TodoResponse;
import com.cabybara.prolearningplatform.enums.TodoPriority;
import com.cabybara.prolearningplatform.enums.TodoStatus;
import com.cabybara.prolearningplatform.enums.TodoType;
import com.cabybara.prolearningplatform.service.todo.TodoService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/todos")
@Tag(name = "Todo APIs")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    @Operation(summary = "Get a list of todos with filters.")
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getAllTodos(
            @RequestParam(required = false) Long goalId,
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) TodoPriority priority,
            @RequestParam(required = false) Boolean noGoal,
            @RequestParam(required = false) TodoType type,
            @RequestParam(required = false) TodoStatus status,
            @ParameterObject @PageableDefault(page = 0, size = 20, sort = "createdAt") Pageable pageable
    ) {
        Page<TodoResponse> page = todoService.getAllTodos(goalId, completed, priority, noGoal, type, status, pageable);
        PaginationResponseDto pagination = PaginationResponseDto.builder()
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalItems(page.getTotalElements())
                .pageSize(page.getSize())
                .build();
        return ResponseEntity.ok(ResponseUtil.success("Successfully", page.getContent(), pagination));
    }

    @Operation(summary = "Get the details of a todo")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{todoId}")
    public ResponseEntity<ApiResponse<TodoResponse>> getTodoById(@PathVariable Long todoId) {
        return ResponseEntity.ok(ResponseUtil.success("Successfully", todoService.getTodoById(todoId), null));
    }

    @Operation(summary = "Create a new todo (may not include a goal)")
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<ApiResponse<TodoResponse>> createTodo(@Valid @RequestBody CreateTodoRequest request) {
        TodoResponse response = todoService.createTodo(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseUtil.success("Todo created successfully", response, null));
    }

    @Operation(summary = "Update todo (can add/remove goals)")
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{todoId}")
    public ResponseEntity<ApiResponse<TodoResponse>> updateTodo(
            @PathVariable Long todoId,
            @Valid @RequestBody UpdateTodoRequest request
    ) {
        return ResponseEntity.ok(ResponseUtil.success("Todo updated successfully", todoService.updateTodo(todoId, request), null));
    }

    @Operation(summary = "Delete todo")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{todoId}")
    public ResponseEntity<ApiResponse<Object>> deleteTodo(@PathVariable Long todoId) {
        todoService.deleteTodo(todoId);
        return ResponseEntity.ok(ResponseUtil.success("Todo deleted successfully", null, null));
    }

    @Operation(summary = "Toggle the completion status of the todo.")
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{todoId}/toggle")
    public ResponseEntity<ApiResponse<TodoResponse>> toggleTodo(@PathVariable Long todoId) {
        return ResponseEntity.ok(ResponseUtil.success("Todo toggled successfully", todoService.toggleTodo(todoId), null));
    }
}
