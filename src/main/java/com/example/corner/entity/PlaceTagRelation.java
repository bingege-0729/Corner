package com.example.corner.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "place_tag_relation")
public class PlaceTagRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 关联ID
    
    @Column(name = "place_id")
    private Long placeId; // 地点ID
    
    @Column(name = "tag_id")
    private Long tagId; // 标签ID
}
