package com.example.corner.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RouteStepVO {
    private Integer sequence; // 第几步，1, 2, 3...
    private String action; // 动作描述，如 "出发前往"、"游玩"
    private PlaceCard place; // 地点卡片信息
    private String travelInfo; // 交通信息，如 "驾车 20分钟" 或 "步行 500米"
    private String timeSlot; // 时间槽，如 "10:00 - 11:30"
    private String aiComment; // AI 针对该步骤的点评
}
