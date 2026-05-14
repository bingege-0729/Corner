package com.example.corner.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户情绪选择记录表
 */
@Data
@Entity
@Table(name = "user_mood_record")
public class UserMoodRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId; // 用户ID
    
    @Column(name = "mood_tag")
    private String moodTag; // 情绪标签（如：好心情、平静、伤心）
    
    @Column(name = "energy_level")
    private Integer energyLevel; // 精力条
    
    @Column(name = "social_level")
    private Integer socialLevel; // 社交欲
    
    @Column(name = "created_at")
    private LocalDateTime createdAt; // 选择时间
}
