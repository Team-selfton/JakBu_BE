package com.jakbu.repository;

import com.jakbu.domain.Todo;
import com.jakbu.domain.enums.TodoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {
    @Query("SELECT t FROM Todo t WHERE t.user.id = :userId AND t.date = :date")
    List<Todo> findByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("SELECT COUNT(t) > 0 FROM Todo t WHERE t.user.id = :userId AND t.date = :date AND t.status = :status")
    boolean existsByUserIdAndDateAndStatus(@Param("userId") Long userId, @Param("date") LocalDate date, @Param("status") TodoStatus status);
    
    default boolean existsTodoToday(Long userId, LocalDate today) {
        return existsByUserIdAndDateAndStatus(userId, today, TodoStatus.TODO);
    }
}

