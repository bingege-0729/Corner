package com.example.corner.service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 百度地图API服务接口
 */
public interface BaiduMapService {

    /**
     * 逆地理编码：将经纬度转换为详细地址
     * 
     * @param latitude
     *            纬度
     * @param longitude
     *            经度
     * @return 地址信息（包含省、市、区、街道等）
     */
    Map<String, Object> reverseGeocoding(BigDecimal latitude, BigDecimal longitude);

    /**
     * 根据IP获取位置
     * 
     * @param ip
     *            IP地址（可选，为空则自动检测）
     * @return 位置信息（包含纬度、经度、地址等）
     */
    Map<String, Object> getLocationByIp(String ip);

    /**
     * 计算两点之间的距离
     * 
     * @param lat1
     *            起点纬度
     * @param lng1
     *            起点经度
     * @param lat2
     *            终点纬度
     * @param lng2
     *            终点经度
     * @return 距离（米）
     */
    double calculateDistance(BigDecimal lat1, BigDecimal lng1, BigDecimal lat2, BigDecimal lng2);
}
