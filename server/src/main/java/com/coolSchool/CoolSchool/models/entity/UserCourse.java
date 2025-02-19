package com.coolSchool.CoolSchool.models.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "users_courses")
public class UserCourse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotNull(message = "The user of the userCourse should not be null!")
    private User user;
    @ManyToOne
    @JoinColumn(name = "course_id")
    @NotNull(message = "The user of the course should not be null!")
    private Course course;
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;
}
