package com.jakbu.controller;

import com.jakbu.dto.TodoRequest;
import com.jakbu.dto.TodoResponse;
import com.jakbu.dto.TodoStatusUpdateRequest;
import com.jakbu.service.TodoService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/todo")
public class TodoController {

    private static final Logger logger = LoggerFactory.getLogger(TodoController.class);
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
    public ResponseEntity<?> getTodosByDate(
            @RequestParam(required = false) String date,
            Authentication authentication) {
        try {
            logger.info("GET /todo/date called with date parameter: {}", date);
            
            if (date == null || date.isEmpty()) {
                logger.warn("Date parameter is missing");
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Date parameter is required", 
                                     "error", "INVALID_DATE",
                                     "status", HttpStatus.BAD_REQUEST.value()));
            }

            LocalDate parsedDate;
            try {
                parsedDate = LocalDate.parse(date);
            } catch (Exception e) {
                logger.error("Failed to parse date: {}", date, e);
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Invalid date format. Expected format: YYYY-MM-DD", 
                                     "error", "INVALID_DATE_FORMAT",
                                     "status", HttpStatus.BAD_REQUEST.value()));
            }

            Long userId = (Long) authentication.getPrincipal();
            logger.info("Fetching todos for userId: {}, date: {}", userId, parsedDate);
            
            List<TodoResponse> todos = todoService.getTodosByDate(userId, parsedDate);
            logger.info("Found {} todos for userId: {}, date: {}", todos.size(), userId, parsedDate);
            
            return ResponseEntity.ok(todos);
        } catch (Exception e) {
            logger.error("Error fetching todos by date", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "서버에서 데이터를 가져오는 중 오류가 발생했습니다.", 
                                 "error", e.getMessage() != null ? e.getMessage() : "UNKNOWN_ERROR",
                                 "status", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @PostMapping("/{id}/done")
    public ResponseEntity<TodoResponse> markTodoDone(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        TodoResponse response = todoService.markTodoDone(userId, id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<TodoResponse> updateTodoStatus(
            @PathVariable Long id,
            @Valid @RequestBody TodoStatusUpdateRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        TodoResponse response = todoService.updateTodoStatus(userId, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        todoService.deleteTodo(userId, id);
        return ResponseEntity.noContent().build();
    }
}

