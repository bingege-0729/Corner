package com.example.corner.vo;

import lombok.Data;
import java.util.Map;

@Data
public class UserStatsResponse {
    private Long visitedPlacesCount;      // 去过的不同地点数量
    private Map<String, Integer> moodStats; // 各情绪标签使用次数 {心情: 次数}
}
