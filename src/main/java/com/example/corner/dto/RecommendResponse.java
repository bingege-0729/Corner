package com.example.corner.dto;

import lombok.Data;
import java.util.List;

@Data
public class RecommendResponse {
    private String understanding;
    private List<PlaceCard> memoryMatches;
    private List<PlaceCard> emotionMatches;
}
