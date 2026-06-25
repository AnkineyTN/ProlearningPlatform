package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.todo.CreateTodoRequest;
import com.cabybara.prolearningplatform.dto.response.todo.TodoResponse;
import com.cabybara.prolearningplatform.enums.TodoPriority;
import com.cabybara.prolearningplatform.enums.TodoStatus;
import com.cabybara.prolearningplatform.enums.TodoType;
import com.cabybara.prolearningplatform.service.todo.TodoService;
import com.cabybara.prolearningplatform.support.WebMvcTestSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TodoController.class)
@Import(WebMvcTestSecurityConfig.class)
@ActiveProfiles("test")
class TodoControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TodoService todoService;

    private TodoResponse todoResponse(Long id) {
        return TodoResponse.builder()
                .id(id)
                .title("Todo " + id)
                .completed(false)
                .priority(TodoPriority.MEDIUM)
                .type(TodoType.DAILY)
                .status(TodoStatus.TODO)
                .build();
    }

    @Test
    void getAllTodosWithoutAuthenticationReturns401() throws Exception {
        mockMvc.perform(get("/todos"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(todoService);
    }

    @Test
    void getAllTodosReturns200WithPaginationMetadata() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        Page<TodoResponse> page = new PageImpl<>(List.of(todoResponse(1L), todoResponse(2L)), pageable, 2);
        when(todoService.getAllTodos(any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/todos").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.metadata.currentPage").value(0))
                .andExpect(jsonPath("$.metadata.totalItems").value(2))
                .andExpect(jsonPath("$.metadata.pageSize").value(20));
    }

    @Test
    void getTodoByIdReturns200() throws Exception {
        when(todoService.getTodoById(5L)).thenReturn(todoResponse(5L));

        mockMvc.perform(get("/todos/5").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(5))
                .andExpect(jsonPath("$.data.title").value("Todo 5"));
    }

    @Test
    void createTodoReturns201() throws Exception {
        CreateTodoRequest request = new CreateTodoRequest();
        request.setTitle("New todo");
        when(todoService.createTodo(any(CreateTodoRequest.class))).thenReturn(todoResponse(10L));

        mockMvc.perform(post("/todos").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Todo created successfully"))
                .andExpect(jsonPath("$.data.id").value(10));
    }

    @Test
    void createTodoRejectsBlankTitleWith400() throws Exception {
        CreateTodoRequest request = new CreateTodoRequest();
        request.setTitle("   ");

        mockMvc.perform(post("/todos").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));

        verifyNoInteractions(todoService);
    }

    @Test
    void createTodoWithoutAuthenticationReturns401() throws Exception {
        CreateTodoRequest request = new CreateTodoRequest();
        request.setTitle("New todo");

        mockMvc.perform(post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(todoService);
    }

    @Test
    void toggleTodoReturns200() throws Exception {
        when(todoService.toggleTodo(7L)).thenReturn(todoResponse(7L));

        mockMvc.perform(patch("/todos/7/toggle").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Todo toggled successfully"))
                .andExpect(jsonPath("$.data.id").value(7));
    }

    @Test
    void deleteTodoReturns200() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/todos/3").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Todo deleted successfully"));

        verify(todoService).deleteTodo(eq(3L));
    }
}
