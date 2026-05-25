package com.example.corner.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "route_plan")
public class RoutePlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "title")
    private String title; // 路线标题，如"清远周一大冒险"

    @Column(name = "total_duration")
    private Integer totalDuration; // 总耗时(分钟)

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}