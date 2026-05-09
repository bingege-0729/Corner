package com.example.corner.dto;

import lombok.Data;

@Data
public class FeedbackRequest {
    private Long placeId;
    private String action;
    private String feedback;
    private Integer rating;
}
