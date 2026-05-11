package com.example.corner.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RecommendRequest {
    private String mood; //心情
    private Integer energyLevel; //精力条
    private Integer socialLevel; //IE值
    private String userInput; //用户输入
    private BigDecimal userLat; //用户纬度
    private BigDecimal userLng; //用户精度
}
