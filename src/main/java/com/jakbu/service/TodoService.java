package com.jakbu.service;

import com.jakbu.domain.Todo;
import com.jakbu.domain.User;
import com.jakbu.dto.TodoRequest;
import com.jakbu.dto.TodoResponse;
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

        todo.markDone();
        todo = todoRepository.save(todo);

        return new TodoResponse(todo.getId(), todo.getTitle(), todo.getDate(), todo.getStatus());
    }
}

