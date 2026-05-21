package com.cabybara.prolearningplatform.service.todo;

import com.cabybara.prolearningplatform.dto.request.todo.CreateGoalRequest;
import com.cabybara.prolearningplatform.dto.request.todo.UpdateGoalRequest;
import com.cabybara.prolearningplatform.dto.response.todo.GoalResponse;
import com.cabybara.prolearningplatform.dto.response.todo.GoalWithTodosResponse;
import com.cabybara.prolearningplatform.enums.GoalStatus;
import com.cabybara.prolearningplatform.enums.GoalType;
import com.cabybara.prolearningplatform.model.Goal;
import com.cabybara.prolearningplatform.model.Todo;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.repository.GoalRepository;
import com.cabybara.prolearningplatform.repository.TodoRepository;
import com.cabybara.prolearningplatform.service.todo.impl.GoalServiceImpl;
import com.cabybara.prolearningplatform.service.user.UserService;
import com.cabybara.prolearningplatform.support.TestFixtures;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoalServiceImplTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationContext authenticationContext;

    @Test
    void createGoalAppliesDefaultTypeAndStatus() {
        GoalServiceImpl service = new GoalServiceImpl(goalRepository, todoRepository, userService, authenticationContext);
        User user = TestFixtures.user(1L);
        CreateGoalRequest request = new CreateGoalRequest();
        request.setTitle("Learn Spring");
        request.setDescription("desc");
        request.setTargetDate(LocalDate.now().plusDays(10));
        request.setColor("#ff0");
        ArgumentCaptor<Goal> captor = ArgumentCaptor.forClass(Goal.class);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(userService.getUserById(1L)).thenReturn(user);
        when(goalRepository.save(org.mockito.ArgumentMatchers.any(Goal.class))).thenAnswer(invocation -> {
            Goal goal = invocation.getArgument(0);
            goal.setId(5L);
            return goal;
        });
        when(todoRepository.countByGoalId(5L)).thenReturn(0L);
        when(todoRepository.countByGoalIdAndCompleted(5L, true)).thenReturn(0L);
        when(goalRepository.findByParentGoalIdAndUserId(5L, 1L)).thenReturn(java.util.List.of());

        GoalResponse response = service.createGoal(request);

        verify(goalRepository).save(captor.capture());
        Goal saved = captor.getValue();
        assertEquals(GoalType.LONG, saved.getType());
        assertEquals(GoalStatus.IN_PROGRESS, saved.getStatus());
        assertSame(user, saved.getUser());
        assertEquals(5L, response.getId());
        assertEquals(0, response.getProgress());
    }

    @Test
    void createGoalAssignsParentGoalWhenProvided() {
        GoalServiceImpl service = new GoalServiceImpl(goalRepository, todoRepository, userService, authenticationContext);
        User user = TestFixtures.user(1L);
        Goal parent = TestFixtures.goal(9L, user);
        CreateGoalRequest request = new CreateGoalRequest();
        request.setTitle("Short goal");
        request.setType(GoalType.SHORT);
        request.setParentGoalId(9L);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(userService.getUserById(1L)).thenReturn(user);
        when(goalRepository.findByIdAndUserId(9L, 1L)).thenReturn(Optional.of(parent));
        when(goalRepository.save(org.mockito.ArgumentMatchers.any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(todoRepository.countByGoalId(null)).thenReturn(0L);
        when(todoRepository.countByGoalIdAndCompleted(null, true)).thenReturn(0L);

        GoalResponse response = service.createGoal(request);

        assertEquals(GoalType.SHORT, response.getType());
        assertEquals(9L, response.getParentGoalId());
    }

    @Test
    void getGoalByIdCalculatesProgressAndMapsTodos() {
        GoalServiceImpl service = new GoalServiceImpl(goalRepository, todoRepository, userService, authenticationContext);
        User user = TestFixtures.user(1L);
        Goal goal = TestFixtures.goal(6L, user);
        Todo todo1 = TestFixtures.todo(1L, user, goal);
        Todo todo2 = TestFixtures.todo(2L, user, goal);
        todo2.setCompleted(true);
        goal.setTodos(TestFixtures.todos(todo1, todo2));

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(goalRepository.findByIdAndUserId(6L, 1L)).thenReturn(Optional.of(goal));
        when(todoRepository.countByGoalId(6L)).thenReturn(2L);
        when(todoRepository.countByGoalIdAndCompleted(6L, true)).thenReturn(1L);

        GoalWithTodosResponse response = service.getGoalById(6L);

        assertEquals(2L, response.getTotalTodos());
        assertEquals(1L, response.getCompletedTodos());
        assertEquals(50, response.getProgress());
        assertEquals(2, response.getTodos().size());
        assertEquals(6L, response.getTodos().get(0).getGoalId());
    }

    @Test
    void updateGoalClearsParentGoal() {
        GoalServiceImpl service = new GoalServiceImpl(goalRepository, todoRepository, userService, authenticationContext);
        User user = TestFixtures.user(1L);
        Goal parent = TestFixtures.goal(1L, user);
        Goal goal = TestFixtures.goal(2L, user);
        goal.setParentGoal(parent);
        UpdateGoalRequest request = new UpdateGoalRequest();
        request.setClearParentGoal(true);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(goalRepository.findByIdAndUserId(2L, 1L)).thenReturn(Optional.of(goal));
        when(goalRepository.save(goal)).thenReturn(goal);
        when(todoRepository.countByGoalId(2L)).thenReturn(0L);
        when(todoRepository.countByGoalIdAndCompleted(2L, true)).thenReturn(0L);
        when(goalRepository.findByParentGoalIdAndUserId(2L, 1L)).thenReturn(java.util.List.of());

        GoalResponse response = service.updateGoal(2L, request);

        assertNull(goal.getParentGoal());
        assertNull(response.getParentGoalId());
    }

    @Test
    void deleteGoalDetachesTodosAndShortGoalsBeforeDelete() {
        GoalServiceImpl service = new GoalServiceImpl(goalRepository, todoRepository, userService, authenticationContext);
        User user = TestFixtures.user(1L);
        Goal goal = TestFixtures.goal(3L, user);
        Todo todo = TestFixtures.todo(1L, user, goal);
        Goal shortGoal = TestFixtures.goal(4L, user);
        shortGoal.setParentGoal(goal);
        goal.setTodos(TestFixtures.todos(todo));
        goal.setShortGoals(TestFixtures.goals(shortGoal));

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(goalRepository.findByIdAndUserId(3L, 1L)).thenReturn(Optional.of(goal));

        service.deleteGoal(3L);

        assertNull(todo.getGoal());
        assertNull(shortGoal.getParentGoal());
        verify(goalRepository).delete(goal);
    }

    @Test
    void getAllGoalsIncludesShortGoalsForLongGoals() {
        GoalServiceImpl service = new GoalServiceImpl(goalRepository, todoRepository, userService, authenticationContext);
        User user = TestFixtures.user(1L);
        Goal parent = TestFixtures.goal(10L, user);
        Goal child = TestFixtures.goal(11L, user);
        child.setType(GoalType.SHORT);
        child.setParentGoal(parent);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(goalRepository.findByFilters(1L, null, null, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(java.util.List.of(parent), PageRequest.of(0, 10), 1));
        when(todoRepository.countByGoalId(10L)).thenReturn(4L);
        when(todoRepository.countByGoalIdAndCompleted(10L, true)).thenReturn(1L);
        when(goalRepository.findByParentGoalIdAndUserId(10L, 1L)).thenReturn(java.util.List.of(child));
        when(todoRepository.countByGoalId(11L)).thenReturn(2L);
        when(todoRepository.countByGoalIdAndCompleted(11L, true)).thenReturn(2L);

        GoalResponse response = service.getAllGoals(null, null, PageRequest.of(0, 10)).getContent().get(0);

        assertEquals(1, response.getShortGoals().size());
        assertEquals(11L, response.getShortGoals().get(0).getId());
        assertEquals(100, response.getShortGoals().get(0).getProgress());
    }
}
