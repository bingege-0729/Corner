package com.example.corner.service;

import java.math.BigDecimal;
import java.util.Map;

public interface LocationService {
    
    /**
     * 获取用户当前位置
     * @param userId 用户ID
     * @return 位置信息（包含纬度、经度等）
     */
    Map<String, Object> getCurrentLocation(Long userId);
    
    /**
     * 更新用户位置
     * @param userId 用户ID
     * @param latitude 纬度
     * @param longitude 经度
     * @return 是否更新成功
     */
    boolean updateLocation(Long userId, BigDecimal latitude, BigDecimal longitude);
}