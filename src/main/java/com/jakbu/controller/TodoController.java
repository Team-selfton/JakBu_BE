package com.jakbu.controller;

import com.jakbu.dto.TodoRequest;
import com.jakbu.dto.TodoResponse;
import com.jakbu.service.TodoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/todo")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @PostMapping
    public ResponseEntity<TodoResponse> createTodo(
            @Valid @RequestBody TodoRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        TodoResponse response = todoService.createTodo(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/today")
    public ResponseEntity<List<TodoResponse>> getTodayTodos(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<TodoResponse> todos = todoService.getTodayTodos(userId);
        return ResponseEntity.ok(todos);
    }

    @GetMapping("/date")
    public ResponseEntity<List<TodoResponse>> getTodosByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<TodoResponse> todos = todoService.getTodosByDate(userId, date);
        return ResponseEntity.ok(todos);
    }

    @PostMapping("/{id}/done")
    public ResponseEntity<TodoResponse> markTodoDone(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        TodoResponse response = todoService.markTodoDone(userId, id);
        return ResponseEntity.ok(response);
    }
}

