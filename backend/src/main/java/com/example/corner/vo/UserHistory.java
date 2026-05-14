package com.example.corner.vo;

import lombok.Data;

//
@Data
public class UserHistory {
    private Boolean hasVisited;
    private Integer visitCount;
    private String lastVisited;
    private Integer yourRating;
    private String yourFeedback;
}
