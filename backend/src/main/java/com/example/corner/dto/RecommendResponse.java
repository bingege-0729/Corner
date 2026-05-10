package com.example.corner.dto;

import lombok.Data;
import reactor.core.publisher.Flux;

import java.util.List;

@Data
public class RecommendResponse {
    private Flux<String> understanding;
    private List<PlaceCard> memoryMatches;
    private List<PlaceCard> emotionMatches;
}
