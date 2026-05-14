package com.example.corner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String role; // 角色：user 或 assistant
    private String content; // 消息内容
    private Long timestamp; // 时间戳
    
    public ChatResponse(String role, String content) {
        this.role = role;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }
}
