package com.cabybara.prolearningplatform.service.todo;

import com.cabybara.prolearningplatform.dto.request.todo.CreateGoalRequest;
import com.cabybara.prolearningplatform.dto.request.todo.UpdateGoalRequest;
import com.cabybara.prolearningplatform.dto.response.todo.GoalResponse;
import com.cabybara.prolearningplatform.dto.response.todo.GoalWithTodosResponse;
import com.cabybara.prolearningplatform.enums.GoalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GoalService {

    Page<GoalResponse> getAllGoals(GoalStatus status, Pageable pageable);

    GoalWithTodosResponse getGoalById(Long goalId);

    GoalResponse createGoal(CreateGoalRequest request);

    GoalResponse updateGoal(Long goalId, UpdateGoalRequest request);

    void deleteGoal(Long goalId);
}
