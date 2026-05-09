package com.example.corner.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PlaceDetailResponse {
    private Long placeId;
    private String placeName;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private List<String> moodTags;
    private String crowdLevel;
    private String bestTime;
    private String oneSentence;
    private String fullDescription;
    private String imageUrl;
    private String tips;
    private UserHistory yourHistory;
}
