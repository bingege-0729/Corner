package com.example.corner.service.impl;

import com.example.corner.entity.UserInfo;
import com.example.corner.repository.UserInfoRepository;
import com.example.corner.service.BaiduMapService;
import com.example.corner.service.LocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class LocationServiceImpl implements LocationService {

    @Autowired
    private BaiduMapService baiduMapService;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Override
    public Map<String, Object> getCurrentLocation(Long userId) {
        UserInfo user = userInfoRepository.findById(userId).orElse(null);

        if (user == null) {
            log.warn("用户不存在: userId={}", userId);
            return new HashMap<>();
        }

        if (user.getLatitude() == null) {
            log.warn("用户位置未设置: userId={}", userId);
            return new HashMap<>();
        }

        Map<String, Object> location = new HashMap<>();
        location.put("latitude", user.getLatitude());
        location.put("longitude", user.getLongitude());
        location.put("address", user.getAddress());
        location.put("province", user.getProvince());
        location.put("city", user.getCity());
        location.put("district", user.getDistrict());
        location.put("street", user.getStreet());
        location.put("source", user.getLocationSource());
        location.put("updatedAt", user.getUpdatedAt());

        log.info("获取用户位置成功: userId={}, lat={}, lng={}, address={}",
                userId, user.getLatitude(), user.getLongitude(), user.getAddress());

        return location;
    }

    @Override
    public boolean updateLocation(Long userId, BigDecimal latitude, BigDecimal longitude) {
        try {
            log.info("开始更新用户位置: userId={}, lat={}, lng={}", userId, latitude, longitude);

            Map<String, Object> addressInfo = baiduMapService.reverseGeocoding(latitude, longitude);
            log.info("百度地图逆地理编码结果: {}", addressInfo);

            UserInfo user = userInfoRepository.findById(userId).orElse(null);

            if (user == null) {
                log.error("用户不存在: userId={}", userId);
                return false;
            }

            user.setLatitude(latitude);
            user.setLongitude(longitude);
            user.setLocationSource("gps");
            user.setUpdatedAt(LocalDateTime.now());

            if ((Boolean) addressInfo.getOrDefault("success", false)) {
                user.setAddress((String) addressInfo.get("formattedAddress"));
                user.setProvince((String) addressInfo.get("province"));
                user.setCity((String) addressInfo.get("city"));
                user.setDistrict((String) addressInfo.get("district"));
                user.setStreet((String) addressInfo.get("street"));
                log.info("逆地理编码成功，地址信息已保存");
            } else {
                log.warn("逆地理编码失败，只保存经纬度: error={}", addressInfo.get("error"));
            }

            userInfoRepository.save(user);
            log.info("用户位置更新成功: userId={}", userId);

            return true;
        } catch (Exception e) {
            log.error("更新用户位置异常: userId={}", userId, e);
            try {
                UserInfo user = userInfoRepository.findById(userId).orElse(null);
                if (user == null) {
                    return false;
                }

                user.setLatitude(latitude);
                user.setLongitude(longitude);
                user.setLocationSource("gps");
                user.setUpdatedAt(LocalDateTime.now());

                userInfoRepository.save(user);
                return true;
            } catch (Exception ex) {
                log.error("保存位置信息失败", ex);
                return false;
            }
        }
    }
}
