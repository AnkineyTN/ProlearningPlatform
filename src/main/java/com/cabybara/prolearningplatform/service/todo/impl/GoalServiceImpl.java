package com.cabybara.prolearningplatform.service.todo.impl;

import com.cabybara.prolearningplatform.dto.request.todo.CreateGoalRequest;
import com.cabybara.prolearningplatform.dto.request.todo.UpdateGoalRequest;
import com.cabybara.prolearningplatform.dto.response.todo.GoalResponse;
import com.cabybara.prolearningplatform.dto.response.todo.GoalWithTodosResponse;
import com.cabybara.prolearningplatform.dto.response.todo.TodoResponse;
import com.cabybara.prolearningplatform.enums.GoalStatus;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.model.Goal;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.repository.GoalRepository;
import com.cabybara.prolearningplatform.repository.TodoRepository;
import com.cabybara.prolearningplatform.service.todo.GoalService;
import com.cabybara.prolearningplatform.service.user.UserService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;
    private final TodoRepository todoRepository;
    private final UserService userService;
    private final AuthenticationContext authenticationContext;

    @Override
    public Page<GoalResponse> getAllGoals(GoalStatus status, Pageable pageable) {
        Long userId = authenticationContext.getCurrentUserId();
        Page<Goal> goals = (status != null)
                ? goalRepository.findByUserIdAndStatus(userId, status, pageable)
                : goalRepository.findByUserId(userId, pageable);
        return goals.map(this::toGoalResponse);
    }

    @Override
    public GoalWithTodosResponse getGoalById(Long goalId) {
        Long userId = authenticationContext.getCurrentUserId();
        Goal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        long total = todoRepository.countByGoalId(goalId);
        long completed = todoRepository.countByGoalIdAndCompleted(goalId, true);

        List<TodoResponse> todos = goal.getTodos().stream()
                .map(todo -> TodoResponse.builder()
                        .id(todo.getId())
                        .title(todo.getTitle())
                        .description(todo.getDescription())
                        .completed(todo.getCompleted())
                        .priority(todo.getPriority())
                        .dueDate(todo.getDueDate())
                        .completedAt(todo.getCompletedAt())
                        .goalId(goalId)
                        .goalTitle(goal.getTitle())
                        .goalColor(goal.getColor())
                        .createdAt(todo.getCreatedAt())
                        .updatedAt(todo.getUpdatedAt())
                        .build())
                .toList();

        return GoalWithTodosResponse.builder()
                .id(goal.getId())
                .title(goal.getTitle())
                .description(goal.getDescription())
                .targetDate(goal.getTargetDate())
                .color(goal.getColor())
                .status(goal.getStatus())
                .totalTodos(total)
                .completedTodos(completed)
                .progress(total == 0 ? 0 : (int) (completed * 100 / total))
                .todos(todos)
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public GoalResponse createGoal(CreateGoalRequest request) {
        Long userId = authenticationContext.getCurrentUserId();
        User user = userService.getUserById(userId);

        Goal goal = Goal.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .targetDate(request.getTargetDate())
                .color(request.getColor())
                .status(GoalStatus.IN_PROGRESS)
                .user(user)
                .build();

        return toGoalResponse(goalRepository.save(goal));
    }

    @Override
    @Transactional
    public GoalResponse updateGoal(Long goalId, UpdateGoalRequest request) {
        Long userId = authenticationContext.getCurrentUserId();
        Goal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        if (request.getTitle() != null) goal.setTitle(request.getTitle());
        if (request.getDescription() != null) goal.setDescription(request.getDescription());
        if (request.getTargetDate() != null) goal.setTargetDate(request.getTargetDate());
        if (request.getColor() != null) goal.setColor(request.getColor());
        if (request.getStatus() != null) goal.setStatus(request.getStatus());

        return toGoalResponse(goalRepository.save(goal));
    }

    @Override
    @Transactional
    public void deleteGoal(Long goalId) {
        Long userId = authenticationContext.getCurrentUserId();
        Goal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
        goal.getTodos().forEach(todo -> todo.setGoal(null));
        goalRepository.delete(goal);
    }

    private GoalResponse toGoalResponse(Goal goal) {
        long total = todoRepository.countByGoalId(goal.getId());
        long completed = todoRepository.countByGoalIdAndCompleted(goal.getId(), true);
        return GoalResponse.builder()
                .id(goal.getId())
                .title(goal.getTitle())
                .description(goal.getDescription())
                .targetDate(goal.getTargetDate())
                .color(goal.getColor())
                .status(goal.getStatus())
                .totalTodos(total)
                .completedTodos(completed)
                .progress(total == 0 ? 0 : (int) (completed * 100 / total))
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .build();
    }
}
