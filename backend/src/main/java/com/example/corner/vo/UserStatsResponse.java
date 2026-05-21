package com.example.corner.vo;

import lombok.Data;
import java.util.Map;

@Data
public class UserStatsResponse {
    private String nickname; // 用户昵称
    private String avatarUrl; // 用户头像URL
    private Long visitedPlacesCount; // 去过的不同地点数量
    private Map<String, Integer> moodStats; // 各情绪标签使用次数 {心情: 次数}
}
