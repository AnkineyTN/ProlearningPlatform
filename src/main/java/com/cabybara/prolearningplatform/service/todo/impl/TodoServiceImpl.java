package com.cabybara.prolearningplatform.service.todo.impl;

import com.cabybara.prolearningplatform.dto.request.todo.CreateTodoRequest;
import com.cabybara.prolearningplatform.dto.request.todo.UpdateTodoRequest;
import com.cabybara.prolearningplatform.dto.response.todo.TodoResponse;
import com.cabybara.prolearningplatform.enums.TodoPriority;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.model.Goal;
import com.cabybara.prolearningplatform.model.Todo;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.repository.GoalRepository;
import com.cabybara.prolearningplatform.repository.TodoRepository;
import com.cabybara.prolearningplatform.service.todo.TodoService;
import com.cabybara.prolearningplatform.service.user.UserService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepository;
    private final GoalRepository goalRepository;
    private final UserService userService;
    private final AuthenticationContext authenticationContext;

    @Override
    public Page<TodoResponse> getAllTodos(Long goalId, Boolean completed, TodoPriority priority, Boolean noGoal, Pageable pageable) {
        Long userId = authenticationContext.getCurrentUserId();
        return todoRepository.findByFilters(userId, goalId, completed, priority, noGoal, pageable)
                .map(this::toTodoResponse);
    }

    @Override
    public TodoResponse getTodoById(Long todoId) {
        Long userId = authenticationContext.getCurrentUserId();
        Todo todo = todoRepository.findByIdAndUserId(todoId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found"));
        return toTodoResponse(todo);
    }

    @Override
    @Transactional
    public TodoResponse createTodo(CreateTodoRequest request) {
        Long userId = authenticationContext.getCurrentUserId();
        User user = userService.getUserById(userId);

        Goal goal = null;
        if (request.getGoalId() != null) {
            goal = goalRepository.findByIdAndUserId(request.getGoalId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
        }

        Todo todo = Todo.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority() != null ? request.getPriority() : TodoPriority.MEDIUM)
                .dueDate(request.getDueDate())
                .completed(false)
                .user(user)
                .goal(goal)
                .build();

        return toTodoResponse(todoRepository.save(todo));
    }

    @Override
    @Transactional
    public TodoResponse updateTodo(Long todoId, UpdateTodoRequest request) {
        Long userId = authenticationContext.getCurrentUserId();
        Todo todo = todoRepository.findByIdAndUserId(todoId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found"));

        if (request.getTitle() != null) todo.setTitle(request.getTitle());
        if (request.getDescription() != null) todo.setDescription(request.getDescription());
        if (request.getPriority() != null) todo.setPriority(request.getPriority());
        if (request.getDueDate() != null) todo.setDueDate(request.getDueDate());

        if (request.getCompleted() != null) {
            todo.setCompleted(request.getCompleted());
            todo.setCompletedAt(request.getCompleted() ? OffsetDateTime.now() : null);
        }

        if (request.isClearGoal()) {
            todo.setGoal(null);
        } else if (request.getGoalId() != null) {
            Goal goal = goalRepository.findByIdAndUserId(request.getGoalId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
            todo.setGoal(goal);
        }

        return toTodoResponse(todoRepository.save(todo));
    }

    @Override
    @Transactional
    public void deleteTodo(Long todoId) {
        Long userId = authenticationContext.getCurrentUserId();
        Todo todo = todoRepository.findByIdAndUserId(todoId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found"));
        todoRepository.delete(todo);
    }

    @Override
    @Transactional
    public TodoResponse toggleTodo(Long todoId) {
        Long userId = authenticationContext.getCurrentUserId();
        Todo todo = todoRepository.findByIdAndUserId(todoId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found"));

        boolean newState = !todo.getCompleted();
        todo.setCompleted(newState);
        todo.setCompletedAt(newState ? OffsetDateTime.now() : null);

        return toTodoResponse(todoRepository.save(todo));
    }

    private TodoResponse toTodoResponse(Todo todo) {
        Goal goal = todo.getGoal();
        return TodoResponse.builder()
                .id(todo.getId())
                .title(todo.getTitle())
                .description(todo.getDescription())
                .completed(todo.getCompleted())
                .priority(todo.getPriority())
                .dueDate(todo.getDueDate())
                .completedAt(todo.getCompletedAt())
                .goalId(goal != null ? goal.getId() : null)
                .goalTitle(goal != null ? goal.getTitle() : null)
                .goalColor(goal != null ? goal.getColor() : null)
                .createdAt(todo.getCreatedAt())
                .updatedAt(todo.getUpdatedAt())
                .build();
    }
}
