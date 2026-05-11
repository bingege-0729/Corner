package com.example.corner.dto;

import lombok.Data;

import java.util.List;

@Data
public class RecommendResponse {
    private String understanding; // 大模型输出的理解和分析
    private List<PlaceCard> emotionMatches; // 心情匹配的地点
}
