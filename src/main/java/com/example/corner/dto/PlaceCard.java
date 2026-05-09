package com.example.corner.dto;

import lombok.Data;
import java.util.List;

@Data
public class PlaceCard {
    private Long placeId;
    private String placeName;
    private String address;
    private List<String> moodTags;
    private String crowdLevel;
    private String oneSentence;
    private String imageUrl;
    private String distanceText;
    private String matchType;
    private String matchReason;
    private String lastVisited;
}
