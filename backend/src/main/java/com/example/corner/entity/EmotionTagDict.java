package com.example.corner.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "emotion_tag")
public class EmotionTagDict {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 标签ID
    
    @Column(name = "tag_name")
    private String tagName; // 标签名称
    
    private String category; // 分类（氛围/功能/场景）
}
