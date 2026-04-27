package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.todo.CreateGoalRequest;
import com.cabybara.prolearningplatform.dto.request.todo.UpdateGoalRequest;
import com.cabybara.prolearningplatform.dto.response.PaginationResponseDto;
import com.cabybara.prolearningplatform.dto.response.todo.GoalResponse;
import com.cabybara.prolearningplatform.dto.response.todo.GoalWithTodosResponse;
import com.cabybara.prolearningplatform.enums.GoalStatus;
import com.cabybara.prolearningplatform.enums.GoalType;
import com.cabybara.prolearningplatform.service.todo.GoalService;
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
@RequestMapping("/goals")
@Tag(name = "Goal APIs")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @Operation(summary = "Get the user's list of goals.")
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getAllGoals(
            @RequestParam(required = false) GoalStatus status,
            @RequestParam(required = false) GoalType type,
            @ParameterObject @PageableDefault(page = 0, size = 20, sort = "createdAt") Pageable pageable
    ) {
        Page<GoalResponse> page = goalService.getAllGoals(status, type, pageable);
        PaginationResponseDto pagination = PaginationResponseDto.builder()
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalItems(page.getTotalElements())
                .pageSize(page.getSize())
                .build();
        return ResponseEntity.ok(ResponseUtil.success("Successfully", page.getContent(), pagination));
    }

    @Operation(summary = "Get details of a goal along with a list of todos.")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{goalId}")
    public ResponseEntity<ApiResponse<GoalWithTodosResponse>> getGoalById(@PathVariable Long goalId) {
        return ResponseEntity.ok(ResponseUtil.success("Successfully", goalService.getGoalById(goalId), null));
    }

    @Operation(summary = "Create new goal")
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<ApiResponse<GoalResponse>> createGoal(@Valid @RequestBody CreateGoalRequest request) {
        GoalResponse response = goalService.createGoal(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseUtil.success("Goal created successfully", response, null));
    }

    @Operation(summary = "Update goal")
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{goalId}")
    public ResponseEntity<ApiResponse<GoalResponse>> updateGoal(
            @PathVariable Long goalId,
            @Valid @RequestBody UpdateGoalRequest request
    ) {
        return ResponseEntity.ok(ResponseUtil.success("Goal updated successfully", goalService.updateGoal(goalId, request), null));
    }

    @Operation(summary = "Delete goal (todos in goal will not be deleted)")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{goalId}")
    public ResponseEntity<ApiResponse<Object>> deleteGoal(@PathVariable Long goalId) {
        goalService.deleteGoal(goalId);
        return ResponseEntity.ok(ResponseUtil.success("Goal deleted successfully", null, null));
    }
}
