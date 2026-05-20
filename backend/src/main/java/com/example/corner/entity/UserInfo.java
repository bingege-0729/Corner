package com.example.corner.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user")
public class UserInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 用户ID
    
    @Column(unique = true, nullable = false)
    private String phone; // 手机号
    
    private String nickname; // 昵称

    @Column(nullable = false)
    private String password;

    private String avatarUrl; // 头像URL路径

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude; // 纬度
    
    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude; // 经度
    
    private String address; // 详细地址
    
    private String province; // 省份
    
    private String city; // 城市
    
    private String district; // 区县
    
    private String street; // 街道
    
    @Column(name = "location_source")
    private String locationSource; // 位置来源：gps/ip/default
    
    @Column(name = "created_at")
    private LocalDateTime createdAt; // 创建时间
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 更新时间
}
