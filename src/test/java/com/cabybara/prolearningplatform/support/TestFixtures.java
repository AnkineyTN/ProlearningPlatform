package com.cabybara.prolearningplatform.support;

import com.cabybara.prolearningplatform.enums.FCMPlatform;
import com.cabybara.prolearningplatform.enums.GoalStatus;
import com.cabybara.prolearningplatform.enums.GoalType;
import com.cabybara.prolearningplatform.enums.NotificationType;
import com.cabybara.prolearningplatform.enums.TodoPriority;
import com.cabybara.prolearningplatform.enums.TodoStatus;
import com.cabybara.prolearningplatform.enums.TodoType;
import com.cabybara.prolearningplatform.model.Goal;
import com.cabybara.prolearningplatform.model.Todo;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.model.fcm.DeviceToken;
import com.cabybara.prolearningplatform.model.noti.Notification;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class TestFixtures {

    private TestFixtures() {
    }

    public static User user(Long id) {
        User user = User.builder()
                .email("user" + id + "@example.com")
                .lastName("Tester")
                .password("secret")
                .build();
        user.setId(id);
        user.setCreatedAt(OffsetDateTime.now().minusDays(5));
        user.setUpdatedAt(OffsetDateTime.now().minusDays(1));
        return user;
    }

    public static Goal goal(Long id, User user) {
        Goal goal = Goal.builder()
                .title("Goal " + id)
                .description("Goal description " + id)
                .targetDate(LocalDate.now().plusDays(7))
                .color("#123456")
                .status(GoalStatus.IN_PROGRESS)
                .type(GoalType.LONG)
                .user(user)
                .shortGoals(new ArrayList<>())
                .todos(new ArrayList<>())
                .build();
        goal.setId(id);
        goal.setCreatedAt(OffsetDateTime.now().minusDays(4));
        goal.setUpdatedAt(OffsetDateTime.now().minusDays(2));
        return goal;
    }

    public static Todo todo(Long id, User user, Goal goal) {
        Todo todo = Todo.builder()
                .title("Todo " + id)
                .description("Todo description " + id)
                .completed(false)
                .priority(TodoPriority.MEDIUM)
                .dueDate(LocalDate.now().plusDays(1))
                .type(TodoType.DAILY)
                .status(TodoStatus.TODO)
                .setRefs(new ArrayList<>())
                .noteRefs(new ArrayList<>())
                .flashcardRefs(new ArrayList<>())
                .examRefs(new ArrayList<>())
                .user(user)
                .goal(goal)
                .build();
        todo.setId(id);
        todo.setCreatedAt(OffsetDateTime.now().minusDays(3));
        todo.setUpdatedAt(OffsetDateTime.now().minusDays(1));
        return todo;
    }

    public static DeviceToken deviceToken(Long id, User user, String token) {
        return DeviceToken.builder()
                .id(id)
                .user(user)
                .token(token)
                .deviceId("device-" + id)
                .platform(FCMPlatform.WEB)
                .lastActiveAt(OffsetDateTime.now().minusHours(1))
                .build();
    }

    public static Notification notification(Long id, User user, NotificationType type, boolean isRead) {
        return Notification.builder()
                .id(id)
                .user(user)
                .type(type)
                .title(type.getDefaultTitle())
                .message("message-" + id)
                .data(Map.of("id", id))
                .actionUrl("/notifications/" + id)
                .isRead(isRead)
                .createdAt(OffsetDateTime.now().minusDays(1))
                .readAt(isRead ? OffsetDateTime.now().minusHours(2) : null)
                .pushSent(false)
                .build();
    }

    public static List<Todo> todos(Todo... todos) {
        return new ArrayList<>(List.of(todos));
    }

    public static List<Goal> goals(Goal... goals) {
        return new ArrayList<>(List.of(goals));
    }
}
