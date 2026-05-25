package com.example.corner.dto;

import com.example.corner.vo.PlaceCard;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class RecommendRequest {
    private String mood; // 心情
    private Integer energyLevel; // 精力条
    private Integer socialLevel; // 社交欲
    private String userInput; // 用户输入（可用于推荐或对话）
    private BigDecimal userLat; // 用户纬度
    private BigDecimal userLng; // 用户精度
    private Boolean enableStream; // 是否启用流式输出（用于对话模式）
    private List<PlaceCard> existingPlaces; // 已有的地点列表（用于路线规划，避免重复调用AI）
}
