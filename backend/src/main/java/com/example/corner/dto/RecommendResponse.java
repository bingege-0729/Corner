package com.example.corner.dto;

import lombok.Data;
import reactor.core.publisher.Flux;

import java.util.List;

@Data
public class RecommendResponse {
    private Flux<String> understanding; //大模型输出
    private List<PlaceCard> emotionMatches; //心情匹配的地点
}
