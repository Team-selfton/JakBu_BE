package com.jakbu.domain;

import com.jakbu.domain.enums.TodoStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "todos")
@Getter
@NoArgsConstructor
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TodoStatus status;

    public Todo(User user, String title, LocalDate date) {
        this.user = user;
        this.title = title;
        this.date = date;
        this.status = TodoStatus.TODO;
    }

    public void markDone() {
        this.status = TodoStatus.DONE;
    }
}

