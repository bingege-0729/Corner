package com.example.corner.dto;

import lombok.Data;
import java.util.List;

@Data
public class PlaceCard {
    private Long placeId; // 地点ID
    private String placeName; // 地点名称
    private String address; // 地点地址
    private List<String> moodTags; // 心情标签
    private String crowdLevel; // 人流量
    private String oneSentence; // 一句话推荐
    private String imageUrl; // 图片URL
    private String distanceText;  // 距离文本
    private String matchType; // 匹配类型 （收藏/历史/不感兴趣）
    private String matchReason; // 匹配原因
    private String lastVisited; // 最后访问时间
}
