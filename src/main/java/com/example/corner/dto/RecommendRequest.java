package com.example.corner.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RecommendRequest {
    private String mood;
    private Integer energyLevel;
    private Integer socialLevel;
    private String userInput;
    private BigDecimal userLat;
    private BigDecimal userLng;
}
