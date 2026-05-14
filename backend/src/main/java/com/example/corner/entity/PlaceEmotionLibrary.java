package com.example.corner.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 地点emos库
 */
@Data
@Entity
@Table(name = "place")
public class PlaceEmotionLibrary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 地点ID
    
    @Column(name = "place_name")
    private String placeName; // 地点名称
    
    private String address; // 地址
    
    private BigDecimal latitude; // 纬度
    
    private BigDecimal longitude; // 经度
    
    @Column(name = "crowd_level")
    private String crowdLevel; // 人流程度（低/中/高）
    
    @Column(name = "best_time")
    private String bestTime; // 最佳时间
    
    @Column(name = "one_sentence")
    private String oneSentence; // 一句话描述
    
    @Column(name = "full_description")
    private String fullDescription; // 完整描述
    
    @Column(name = "image_url")
    private String imageUrl; // 图片URL
    
    private String tips; // 小贴士
}
