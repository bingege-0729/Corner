package com.example.corner.controller;

import com.example.corner.common.Result;
import com.example.corner.service.BaiduMapService;
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

    @Autowired
    private BaiduMapService baiduMapService;

    @GetMapping("/current")
    public Result<Map<String, Object>> getCurrentLocation(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("获取用户位置: userId={}", userId);
        Map<String, Object> location = locationService.getCurrentLocation(userId);
        return Result.success(location);
    }

    @PostMapping("/update")
    public Result<String> updateLocation(HttpServletRequest request,
                                         @RequestBody(required = false) Map<String, Object> locationData) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("接收到位置更新请求: userId={}, body={}", userId, locationData);

        if (userId == null) {
            log.warn("userId 为空，请先登录");
            return Result.error(401, "请先登录");
        }

        if (locationData == null || locationData.get("latitude") == null || locationData.get("longitude") == null) {
            log.warn("请求体缺少 latitude 或 longitude");
            return Result.error(400, "请求体必须包含 latitude 和 longitude 字段");
        }

        BigDecimal latitude;
        BigDecimal longitude;
        try {
            latitude = new BigDecimal(locationData.get("latitude").toString());
            longitude = new BigDecimal(locationData.get("longitude").toString());
        } catch (NumberFormatException e) {
            log.warn("经纬度格式错误: {}", locationData);
            return Result.error(400, "经纬度必须为有效数字");
        }

        boolean success = locationService.updateLocation(userId, latitude, longitude);
        if (success) {
            return Result.success("位置更新成功");
        } else {
            return Result.error(500, "位置更新失败");
        }
    }

    @GetMapping("/reverse-geocoding")
    public Result<Map<String, Object>> reverseGeocoding(
            @RequestParam BigDecimal latitude,
            @RequestParam BigDecimal longitude) {
        Map<String, Object> addressInfo = baiduMapService.reverseGeocoding(latitude, longitude);

        if ((Boolean) addressInfo.getOrDefault("success", false)) {
            return Result.success(addressInfo);
        } else {
            return Result.error(500, addressInfo.get("error").toString());
        }
    }

    @GetMapping("/ip-location")
    public Result<Map<String, Object>> getLocationByIp(
            @RequestParam(required = false) String ip,
            HttpServletRequest request) {

        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }

        Map<String, Object> locationInfo = baiduMapService.getLocationByIp(ip);

        if ((Boolean) locationInfo.getOrDefault("success", false)) {
            return Result.success(locationInfo);
        } else {
            return Result.error(500, locationInfo.get("error").toString());
        }
    }
}
