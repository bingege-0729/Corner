package com.example.corner.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_place_memory")
public class UserPlaceMemory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 记忆ID
    
    @Column(name = "user_id")
    private Long userId; // 用户ID
    
    @Column(name = "place_id")
    private Long placeId; // 地点ID
    
    @Column(name = "interaction_type")
    private String interactionType; // 互动类型（VISITED/BOOKMARKED/DISLIKED）
    
    private Integer rating; // 评分（1-5）
    
    private String feedback; // 反馈内容
    
    @Column(name = "visited_at")
    private LocalDate visitedAt; // 访问日期
    
    @Column(name = "created_at")
    private LocalDateTime createdAt; // 创建时间
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 更新时间
}
