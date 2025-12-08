package com.jakbu.service;

import com.jakbu.domain.Todo;
import com.jakbu.domain.User;
import com.jakbu.domain.enums.TodoStatus;
import com.jakbu.dto.TodoRequest;
import com.jakbu.dto.TodoResponse;
import com.jakbu.dto.TodoStatusUpdateRequest;
import com.jakbu.repository.TodoRepository;
import com.jakbu.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TodoService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;

    public TodoService(TodoRepository todoRepository, UserRepository userRepository) {
        this.todoRepository = todoRepository;
        this.userRepository = userRepository;
    }

    public TodoResponse createTodo(Long userId, TodoRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Todo todo = new Todo(user, request.title(), request.date());
        todo = todoRepository.save(todo);

        return new TodoResponse(todo.getId(), todo.getTitle(), todo.getDate(), todo.getStatus());
    }

    @Transactional(readOnly = true)
    public List<TodoResponse> getTodayTodos(Long userId) {
        return getTodosByDate(userId, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<TodoResponse> getTodosByDate(Long userId, LocalDate date) {
        List<Todo> todos = todoRepository.findByUserIdAndDate(userId, date);
        return todos.stream()
                .map(todo -> new TodoResponse(todo.getId(), todo.getTitle(), todo.getDate(), todo.getStatus()))
                .collect(Collectors.toList());
    }

    public TodoResponse markTodoDone(Long userId, Long todoId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new RuntimeException("Todo not found"));

        if (!todo.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        // 상태 토글: TODO -> DONE, DONE -> TODO
        todo.toggleStatus();
        todo = todoRepository.save(todo);

        return new TodoResponse(todo.getId(), todo.getTitle(), todo.getDate(), todo.getStatus());
    }

    public TodoResponse updateTodoStatus(Long userId, Long todoId, TodoStatusUpdateRequest request) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new RuntimeException("Todo not found"));

        if (!todo.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        todo.setDone(request.done());
        todo = todoRepository.save(todo);

        return new TodoResponse(todo.getId(), todo.getTitle(), todo.getDate(), todo.getStatus());
    }

    /**
     * 자정에 이전 날짜의 완료 상태를 초기화 (DONE -> TODO)
     */
    public int resetDoneStatusBefore(LocalDate today) {
        return todoRepository.resetDoneBeforeDate(today, TodoStatus.DONE, TodoStatus.TODO);
    }
}

