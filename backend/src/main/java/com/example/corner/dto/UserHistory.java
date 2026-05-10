package com.example.corner.dto;

import lombok.Data;

@Data
public class UserHistory {
    private Boolean hasVisited;
    private Integer visitCount;
    private String lastVisited;
    private Integer yourRating;
    private String yourFeedback;
}
