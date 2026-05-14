package com.example.corner.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

// 地点详情
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
