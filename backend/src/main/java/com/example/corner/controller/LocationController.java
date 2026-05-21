package com.example.corner.controller;

import com.example.corner.common.Result;
import com.example.corner.service.LocationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/location")
public class LocationController {

    @Autowired
    private LocationService locationService;

    /**
     * 获取用户实时位置
     * 
     * @param request
     * @param locationData
     *            位置信息
     * @return
     */
    @PostMapping("/update")
    public Result<String> updateLocation(HttpServletRequest request, @RequestBody Map<String, Object> locationData) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("接收到位置更新请求: userId={}, data={}", userId, locationData);

        // 参数校验
        if (userId == null) {
            log.warn("userId 为空，请先登录");
            return Result.error(401, "请先登录");
        }

        if (locationData == null || !locationData.containsKey("latitude") || !locationData.containsKey("longitude")) {
            log.warn("请求体缺少 latitude 或 longitude");
            return Result.error(400, "请求体必须包含 latitude 和 longitude 字段");
        }

        BigDecimal latitude;
        BigDecimal longitude;

        try {
            latitude = new BigDecimal(locationData.get("latitude").toString());
            longitude = new BigDecimal(locationData.get("longitude").toString());

            // 验证经纬度范围
            if (latitude.compareTo(new BigDecimal("-90")) < 0 || latitude.compareTo(new BigDecimal("90")) > 0) {
                return Result.error(400, "纬度范围应在 -90 到 90 之间");
            }
            if (longitude.compareTo(new BigDecimal("-180")) < 0 || longitude.compareTo(new BigDecimal("180")) > 0) {
                return Result.error(400, "经度范围应在 -180 到 180 之间");
            }
        } catch (NumberFormatException e) {
            log.warn("经纬度格式错误: {}", locationData);
            return Result.error(400, "经纬度必须为有效数字");
        }

        // 调用服务层更新位置
        boolean success = locationService.updateLocation(userId, latitude, longitude);

        if (success) {
            log.info("位置更新成功: userId={}, lat={}, lng={}", userId, latitude, longitude);
            return Result.success("位置更新成功");
        } else {
            log.error("位置更新失败: userId={}", userId);
            return Result.error(500, "位置更新失败");
        }
    }
}
