package com.cabybara.prolearningplatform.service.todo;

import com.cabybara.prolearningplatform.dto.request.todo.CreateTodoRequest;
import com.cabybara.prolearningplatform.dto.request.todo.UpdateTodoRequest;
import com.cabybara.prolearningplatform.dto.response.todo.TodoResponse;
import com.cabybara.prolearningplatform.enums.TodoPriority;
import com.cabybara.prolearningplatform.enums.TodoStatus;
import com.cabybara.prolearningplatform.enums.TodoType;
import com.cabybara.prolearningplatform.model.Goal;
import com.cabybara.prolearningplatform.model.ResourceRef;
import com.cabybara.prolearningplatform.model.Todo;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.repository.GoalRepository;
import com.cabybara.prolearningplatform.repository.TodoRepository;
import com.cabybara.prolearningplatform.service.calendar.CalendarService;
import com.cabybara.prolearningplatform.service.todo.impl.TodoServiceImpl;
import com.cabybara.prolearningplatform.service.user.UserService;
import com.cabybara.prolearningplatform.support.TestFixtures;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodoServiceImplTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationContext authenticationContext;

    @Mock
    private CalendarService calendarService;

    @Test
    void createTodoAppliesDefaultStatusPriorityTypeAndEmptyRefs() {
        TodoServiceImpl service = new TodoServiceImpl(todoRepository, goalRepository, userService, authenticationContext, calendarService);
        User user = TestFixtures.user(1L);
        CreateTodoRequest request = new CreateTodoRequest();
        request.setTitle("Read docs");
        request.setDescription("desc");
        request.setSetRefs(null);
        request.setNoteRefs(null);
        request.setFlashcardRefs(null);
        request.setExamRefs(null);
        ArgumentCaptor<Todo> captor = ArgumentCaptor.forClass(Todo.class);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(userService.getUserById(1L)).thenReturn(user);
        when(todoRepository.save(org.mockito.ArgumentMatchers.any(Todo.class))).thenAnswer(invocation -> {
            Todo todo = invocation.getArgument(0);
            todo.setId(10L);
            return todo;
        });

        TodoResponse response = service.createTodo(request);

        verify(todoRepository).save(captor.capture());
        Todo saved = captor.getValue();
        assertEquals(TodoStatus.TODO, saved.getStatus());
        assertEquals(TodoPriority.MEDIUM, saved.getPriority());
        assertEquals(TodoType.DAILY, saved.getType());
        assertFalse(saved.getCompleted());
        assertNull(saved.getCompletedAt());
        assertEquals(List.of(), saved.getSetRefs());
        assertEquals(List.of(), saved.getNoteRefs());
        assertEquals(List.of(), saved.getFlashcardRefs());
        assertEquals(List.of(), saved.getExamRefs());
        assertSame(user, saved.getUser());
        assertEquals(10L, response.getId());
    }

    @Test
    void createTodoMarksCompletedWhenStatusIsDone() {
        TodoServiceImpl service = new TodoServiceImpl(todoRepository, goalRepository, userService, authenticationContext, calendarService);
        User user = TestFixtures.user(1L);
        Goal goal = TestFixtures.goal(3L, user);
        CreateTodoRequest request = new CreateTodoRequest();
        request.setTitle("Finish task");
        request.setStatus(TodoStatus.DONE);
        request.setPriority(TodoPriority.HIGH);
        request.setType(TodoType.WEEKLY);
        request.setGoalId(3L);
        request.setSetRefs(List.of(new ResourceRef(1L, "Set 1")));

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(userService.getUserById(1L)).thenReturn(user);
        when(goalRepository.findByIdAndUserId(3L, 1L)).thenReturn(Optional.of(goal));
        when(todoRepository.save(org.mockito.ArgumentMatchers.any(Todo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TodoResponse response = service.createTodo(request);

        assertTrue(response.getCompleted());
        assertEquals(TodoStatus.DONE, response.getStatus());
        assertEquals(TodoPriority.HIGH, response.getPriority());
        assertEquals(TodoType.WEEKLY, response.getType());
        assertEquals(3L, response.getGoalId());
        assertNotNull(response.getCompletedAt());
    }

    @Test
    void updateTodoSyncsCompletedStateFromStatusAndClearsGoal() {
        TodoServiceImpl service = new TodoServiceImpl(todoRepository, goalRepository, userService, authenticationContext, calendarService);
        User user = TestFixtures.user(1L);
        Goal goal = TestFixtures.goal(5L, user);
        Todo todo = TestFixtures.todo(10L, user, goal);
        UpdateTodoRequest request = new UpdateTodoRequest();
        request.setStatus(TodoStatus.DONE);
        request.setClearGoal(true);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(todoRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(todo));
        when(todoRepository.save(todo)).thenReturn(todo);

        TodoResponse response = service.updateTodo(10L, request);

        assertTrue(todo.getCompleted());
        assertEquals(TodoStatus.DONE, todo.getStatus());
        assertNotNull(todo.getCompletedAt());
        assertNull(todo.getGoal());
        assertNull(response.getGoalId());
    }

    @Test
    void updateTodoSyncsStatusWhenCompletedFlagProvided() {
        TodoServiceImpl service = new TodoServiceImpl(todoRepository, goalRepository, userService, authenticationContext, calendarService);
        User user = TestFixtures.user(1L);
        Todo todo = TestFixtures.todo(11L, user, null);
        todo.setCompleted(true);
        todo.setStatus(TodoStatus.DONE);
        todo.setCompletedAt(java.time.OffsetDateTime.now().minusDays(1));
        UpdateTodoRequest request = new UpdateTodoRequest();
        request.setCompleted(false);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(todoRepository.findByIdAndUserId(11L, 1L)).thenReturn(Optional.of(todo));
        when(todoRepository.save(todo)).thenReturn(todo);

        TodoResponse response = service.updateTodo(11L, request);

        assertFalse(todo.getCompleted());
        assertEquals(TodoStatus.TODO, todo.getStatus());
        assertNull(todo.getCompletedAt());
        assertFalse(response.getCompleted());
    }

    @Test
    void updateTodoAssignsGoalWhenGoalIdProvided() {
        TodoServiceImpl service = new TodoServiceImpl(todoRepository, goalRepository, userService, authenticationContext, calendarService);
        User user = TestFixtures.user(1L);
        Todo todo = TestFixtures.todo(12L, user, null);
        Goal goal = TestFixtures.goal(8L, user);
        UpdateTodoRequest request = new UpdateTodoRequest();
        request.setGoalId(8L);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(todoRepository.findByIdAndUserId(12L, 1L)).thenReturn(Optional.of(todo));
        when(goalRepository.findByIdAndUserId(8L, 1L)).thenReturn(Optional.of(goal));
        when(todoRepository.save(todo)).thenReturn(todo);

        service.updateTodo(12L, request);

        assertSame(goal, todo.getGoal());
    }

    @Test
    void toggleTodoFlipsCompletedAndStatus() {
        TodoServiceImpl service = new TodoServiceImpl(todoRepository, goalRepository, userService, authenticationContext, calendarService);
        User user = TestFixtures.user(1L);
        Todo todo = TestFixtures.todo(13L, user, null);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(todoRepository.findByIdAndUserId(13L, 1L)).thenReturn(Optional.of(todo));
        when(todoRepository.save(todo)).thenReturn(todo);

        TodoResponse response = service.toggleTodo(13L);

        assertTrue(todo.getCompleted());
        assertEquals(TodoStatus.DONE, todo.getStatus());
        assertNotNull(todo.getCompletedAt());
        assertTrue(response.getCompleted());
    }
}
