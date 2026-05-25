package com.example.corner.vo;

import lombok.Data;
import java.util.List;

//ai
@Data
public class RecommendResponse {
    private String understanding; // AI对用户需求的理解
    private List<PlaceCard> emotionMatches; // 匹配的地点列表
    private List<RouteStepVO> routeSteps; // 路线规划步骤（可选）
}
