package com.cabybara.prolearningplatform.service.todo.impl;

import com.cabybara.prolearningplatform.dto.request.todo.CreateTodoRequest;
import com.cabybara.prolearningplatform.dto.request.todo.UpdateTodoRequest;
import com.cabybara.prolearningplatform.dto.response.todo.TodoResponse;
import com.cabybara.prolearningplatform.enums.TodoPriority;
import com.cabybara.prolearningplatform.enums.TodoStatus;
import com.cabybara.prolearningplatform.enums.TodoType;
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
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepository;
    private final GoalRepository goalRepository;
    private final UserService userService;
    private final AuthenticationContext authenticationContext;

    @Override
    public Page<TodoResponse> getAllTodos(Long goalId, Boolean completed, TodoPriority priority, Boolean noGoal, TodoType type, TodoStatus status, Pageable pageable) {
        Long userId = authenticationContext.getCurrentUserId();
        return todoRepository.findByFilters(userId, goalId, completed, priority, noGoal, type, status, pageable)
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

        TodoStatus status = request.getStatus() != null ? request.getStatus() : TodoStatus.TODO;
        boolean completed = status == TodoStatus.DONE;

        Todo todo = Todo.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority() != null ? request.getPriority() : TodoPriority.MEDIUM)
                .dueDate(request.getDueDate())
                .completed(completed)
                .completedAt(completed ? OffsetDateTime.now() : null)
                .type(request.getType() != null ? request.getType() : TodoType.DAILY)
                .status(status)
                .setRefs(request.getSetRefs() != null ? request.getSetRefs() : new ArrayList<>())
                .noteRefs(request.getNoteRefs() != null ? request.getNoteRefs() : new ArrayList<>())
                .flashcardRefs(request.getFlashcardRefs() != null ? request.getFlashcardRefs() : new ArrayList<>())
                .examRefs(request.getExamRefs() != null ? request.getExamRefs() : new ArrayList<>())
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
        if (request.getType() != null) todo.setType(request.getType());

        if (request.getStatus() != null) {
            todo.setStatus(request.getStatus());
            boolean done = request.getStatus() == TodoStatus.DONE;
            todo.setCompleted(done);
            todo.setCompletedAt(done ? OffsetDateTime.now() : null);
        } else if (request.getCompleted() != null) {
            todo.setCompleted(request.getCompleted());
            todo.setCompletedAt(request.getCompleted() ? OffsetDateTime.now() : null);
            todo.setStatus(request.getCompleted() ? TodoStatus.DONE : TodoStatus.TODO);
        }

        if (request.getSetRefs() != null) todo.setSetRefs(request.getSetRefs());
        if (request.getNoteRefs() != null) todo.setNoteRefs(request.getNoteRefs());
        if (request.getFlashcardRefs() != null) todo.setFlashcardRefs(request.getFlashcardRefs());
        if (request.getExamRefs() != null) todo.setExamRefs(request.getExamRefs());

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

        boolean newDone = !todo.getCompleted();
        todo.setCompleted(newDone);
        todo.setCompletedAt(newDone ? OffsetDateTime.now() : null);
        todo.setStatus(newDone ? TodoStatus.DONE : TodoStatus.TODO);

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
                .type(todo.getType())
                .status(todo.getStatus())
                .setRefs(todo.getSetRefs())
                .noteRefs(todo.getNoteRefs())
                .flashcardRefs(todo.getFlashcardRefs())
                .examRefs(todo.getExamRefs())
                .createdAt(todo.getCreatedAt())
                .updatedAt(todo.getUpdatedAt())
                .build();
    }
}
