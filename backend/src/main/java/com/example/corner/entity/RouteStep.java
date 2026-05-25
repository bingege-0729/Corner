package com.example.corner.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "route_step")
public class RouteStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "route_id")
    private Long routeId; // 属于哪条路线

    @Column(name = "place_id")
    private Long placeId; // 地点ID

    @Column(name = "sequence")
    private Integer sequence; // 第几步，0=起点，1=第一站...

    @Column(name = "travel_mode")
    private String travelMode = "WALKING"; // 交通方式

    @Column(name = "travel_duration")
    private Integer travelDuration; // 路上耗时

    @Column(name = "visit_duration")
    private Integer visitDuration; // 玩耍耗时（取自Place的suggestedDuration）

    // 时间轴
    @Column(name = "arrive_time")
    private LocalDateTime arriveTime; // 预计到达时间

    @Column(name = "leave_time")
    private LocalDateTime leaveTime; // 预计离开时间
}