package com.cabybara.prolearningplatform.service.todo.impl;

import com.cabybara.prolearningplatform.dto.request.todo.CreateGoalRequest;
import com.cabybara.prolearningplatform.dto.request.todo.UpdateGoalRequest;
import com.cabybara.prolearningplatform.dto.response.todo.GoalResponse;
import com.cabybara.prolearningplatform.dto.response.todo.GoalWithTodosResponse;
import com.cabybara.prolearningplatform.dto.response.todo.TodoResponse;
import com.cabybara.prolearningplatform.enums.GoalStatus;
import com.cabybara.prolearningplatform.enums.GoalType;
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

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;
    private final TodoRepository todoRepository;
    private final UserService userService;
    private final AuthenticationContext authenticationContext;

    @Override
    public Page<GoalResponse> getAllGoals(GoalStatus status, GoalType type, Pageable pageable) {
        Long userId = authenticationContext.getCurrentUserId();
        Page<Goal> goals = goalRepository.findByFilters(userId, status, type, pageable);
        return goals.map(g -> toGoalResponse(g, userId));
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
                        .type(todo.getType())
                        .status(todo.getStatus())
                        .setRefs(todo.getSetRefs())
                        .noteRefs(todo.getNoteRefs())
                        .flashcardRefs(todo.getFlashcardRefs())
                        .examRefs(todo.getExamRefs())
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
                .type(goal.getType())
                .parentGoalId(goal.getParentGoal() != null ? goal.getParentGoal().getId() : null)
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

        GoalType goalType = request.getType() != null ? request.getType() : GoalType.LONG;

        Goal parentGoal = null;
        if (request.getParentGoalId() != null) {
            parentGoal = goalRepository.findByIdAndUserId(request.getParentGoalId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent goal not found"));
        }

        Goal goal = Goal.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .targetDate(request.getTargetDate())
                .color(request.getColor())
                .status(GoalStatus.IN_PROGRESS)
                .type(goalType)
                .parentGoal(parentGoal)
                .user(user)
                .build();

        return toGoalResponse(goalRepository.save(goal), userId);
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
        if (request.getType() != null) goal.setType(request.getType());

        if (request.isClearParentGoal()) {
            goal.setParentGoal(null);
        } else if (request.getParentGoalId() != null) {
            Goal parent = goalRepository.findByIdAndUserId(request.getParentGoalId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent goal not found"));
            goal.setParentGoal(parent);
        }

        return toGoalResponse(goalRepository.save(goal), userId);
    }

    @Override
    @Transactional
    public void deleteGoal(Long goalId) {
        Long userId = authenticationContext.getCurrentUserId();
        Goal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
        goal.getTodos().forEach(todo -> todo.setGoal(null));
        goal.getShortGoals().forEach(sg -> sg.setParentGoal(null));
        goalRepository.delete(goal);
    }

    private GoalResponse toGoalResponse(Goal goal, Long userId) {
        long total = todoRepository.countByGoalId(goal.getId());
        long completed = todoRepository.countByGoalIdAndCompleted(goal.getId(), true);

        List<GoalResponse> shortGoals = Collections.emptyList();
        if (goal.getType() == GoalType.LONG) {
            shortGoals = goalRepository.findByParentGoalIdAndUserId(goal.getId(), userId)
                    .stream()
                    .map(sg -> toShortGoalResponse(sg))
                    .toList();
        }

        return GoalResponse.builder()
                .id(goal.getId())
                .title(goal.getTitle())
                .description(goal.getDescription())
                .targetDate(goal.getTargetDate())
                .color(goal.getColor())
                .status(goal.getStatus())
                .type(goal.getType())
                .parentGoalId(goal.getParentGoal() != null ? goal.getParentGoal().getId() : null)
                .totalTodos(total)
                .completedTodos(completed)
                .progress(total == 0 ? 0 : (int) (completed * 100 / total))
                .shortGoals(shortGoals)
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .build();
    }

    private GoalResponse toShortGoalResponse(Goal goal) {
        long total = todoRepository.countByGoalId(goal.getId());
        long completed = todoRepository.countByGoalIdAndCompleted(goal.getId(), true);
        return GoalResponse.builder()
                .id(goal.getId())
                .title(goal.getTitle())
                .description(goal.getDescription())
                .targetDate(goal.getTargetDate())
                .color(goal.getColor())
                .status(goal.getStatus())
                .type(goal.getType())
                .parentGoalId(goal.getParentGoal() != null ? goal.getParentGoal().getId() : null)
                .totalTodos(total)
                .completedTodos(completed)
                .progress(total == 0 ? 0 : (int) (completed * 100 / total))
                .shortGoals(Collections.emptyList())
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .build();
    }
}
