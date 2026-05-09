package com.example.corner.dto;

import lombok.Data;

@Data
public class MemoryItem {
    private Long memoryId;
    private Long placeId;
    private String placeName;
    private String imageUrl;
    private String interactionType;
    private Integer rating;
    private String feedback;
    private String visitedAt;
}
