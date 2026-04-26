package com.cabybara.prolearningplatform.service.todo;

import com.cabybara.prolearningplatform.dto.request.todo.CreateTodoRequest;
import com.cabybara.prolearningplatform.dto.request.todo.UpdateTodoRequest;
import com.cabybara.prolearningplatform.dto.response.todo.TodoResponse;
import com.cabybara.prolearningplatform.enums.TodoPriority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TodoService {

    Page<TodoResponse> getAllTodos(Long goalId, Boolean completed, TodoPriority priority, Boolean noGoal, Pageable pageable);

    TodoResponse getTodoById(Long todoId);

    TodoResponse createTodo(CreateTodoRequest request);

    TodoResponse updateTodo(Long todoId, UpdateTodoRequest request);

    void deleteTodo(Long todoId);

    TodoResponse toggleTodo(Long todoId);
}
