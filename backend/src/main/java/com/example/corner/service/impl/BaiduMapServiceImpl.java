package com.example.corner.service.impl;

import com.example.corner.service.BaiduMapService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 百度地图API服务实现类
 */
@Service
public class BaiduMapServiceImpl implements BaiduMapService {

    @Value("${baidu.map.api-key}")
    private String apiKey;

    @Value("${baidu.map.geocoding-url}")
    private String geocodingUrl;

    @Value("${baidu.map.ip-location-url}")
    private String ipLocationUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 逆地理编码：将经纬度转换为详细地址
     */
    @Override
    public Map<String, Object> reverseGeocoding(BigDecimal latitude, BigDecimal longitude) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 构建请求URL
            String url = String.format("%s?ak=%s&output=json&coordtype=wgs84ll&location=%s,%s", geocodingUrl, apiKey,
                    latitude, longitude);

            // 发送HTTP请求
            String response = restTemplate.getForObject(url, String.class);

            // 解析响应
            JsonNode rootNode = objectMapper.readTree(response);
            int status = rootNode.get("status").asInt();

            if (status == 0) {
                JsonNode resultNode = rootNode.get("result");

                // 提取地址信息
                JsonNode addressComponent = resultNode.get("addressComponent");
                String province = addressComponent.get("province").asText();
                String city = addressComponent.get("city").asText();
                String district = addressComponent.get("district").asText();
                String street = addressComponent.get("street").asText();
                String formattedAddress = resultNode.get("formatted_address").asText();

                result.put("success", true);
                result.put("province", province);
                result.put("city", city);
                result.put("district", district);
                result.put("street", street);
                result.put("formattedAddress", formattedAddress);
                result.put("latitude", latitude);
                result.put("longitude", longitude);
            } else {
                String message = rootNode.has("message") ? rootNode.get("message").asText() : "未知错误";
                result.put("success", false);
                result.put("error", message);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "逆地理编码失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 根据IP获取位置
     */
    @Override
    public Map<String, Object> getLocationByIp(String ip) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 构建请求URL
            String url = String.format("%s?ak=%s&ip=%s&coor=bd09ll", ipLocationUrl, apiKey, ip != null ? ip : "");

            // 发送HTTP请求
            String response = restTemplate.getForObject(url, String.class);

            // 解析响应
            JsonNode rootNode = objectMapper.readTree(response);
            int status = rootNode.get("status").asInt();

            if (status == 0) {
                JsonNode contentNode = rootNode.get("content");
                JsonNode pointNode = contentNode.get("point");

                String x = pointNode.get("x").asText(); // 经度
                String y = pointNode.get("y").asText(); // 纬度

                result.put("success", true);
                result.put("latitude", new BigDecimal(y));
                result.put("longitude", new BigDecimal(x));
                result.put("accuracy", "IP定位");
            } else {
                String message = rootNode.has("message") ? rootNode.get("message").asText() : "未知错误";
                result.put("success", false);
                result.put("error", message);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "IP定位失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 计算两点之间的距离（Haversine公式）
     */
    @Override
    public double calculateDistance(BigDecimal lat1, BigDecimal lng1, BigDecimal lat2, BigDecimal lng2) {
        final int R = 6371000; // 地球半径（米）

        double latDistance = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double lngDistance = Math.toRadians(lng2.doubleValue() - lng1.doubleValue());

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(Math.toRadians(lat1.doubleValue()))
                * Math.cos(Math.toRadians(lat2.doubleValue())) * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // 返回距离（米）
    }
}
