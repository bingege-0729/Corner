package com.example.corner.dto;

import lombok.Data;

@Data
public class UserHistory {
    private Boolean hasVisited; // 是否访问过
    private Integer visitCount; // 访问次数
    private String lastVisited; // 最后访问时间
    private Integer yourRating; // 你的评分
    private String yourFeedback;// 你的反馈
}
