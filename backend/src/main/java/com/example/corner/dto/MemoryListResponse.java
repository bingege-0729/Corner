package com.example.corner.dto;

import lombok.Data;
import java.util.List;

@Data
public class MemoryListResponse {
    private List<MemoryItem> list;
}
