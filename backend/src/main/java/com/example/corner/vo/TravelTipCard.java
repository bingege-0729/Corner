package com.example.corner.vo;

import lombok.Data;

// 出行提示卡
@Data
public class TravelTipCard {
    private Long placeId;              // 地点ID
    private String placeName;          // 地点名称
    private String address;            // 地址
    private String weatherTip;         // 天气提示
    private String preparationTip;     // 准备事项
    private String aiMessage;          // AI温馨寄语
    private String distanceText;       // 距离文本
}
