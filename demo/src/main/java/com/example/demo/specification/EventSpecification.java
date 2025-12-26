package com.example.demo.specification;

import com.example.demo.entity.EventEntity;
import com.example.demo.util.GeoHash;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class EventSpecification {

    // Search theo location (linh hoạt, không phân biệt hoa thường)
    public static Specification<EventEntity> hasLocation(String location) {
        return (root, query, criteriaBuilder) -> {
            if (location == null || location.isEmpty()) return null;
            String pattern = "%" + location.toLowerCase() + "%";
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("location")), pattern);
        };
    }

    // Search theo eventType (linh hoạt, không phân biệt hoa thường)
    public static Specification<EventEntity> hasEventType(String eventType) {
        return (root, query, criteriaBuilder) -> {
            if (eventType == null || eventType.isEmpty()) return null;
            String pattern = "%" + eventType.toLowerCase() + "%";
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("eventType")), pattern);
        };
    }

    // Search các event sau một ngày cụ thể
    public static Specification<EventEntity> afterDate(LocalDate afterDate) {
        return (root, query, criteriaBuilder) ->
                (afterDate == null) ? null : criteriaBuilder.greaterThanOrEqualTo(root.get("date"), afterDate);
    }

    // Search các event trước một ngày cụ thể
    public static Specification<EventEntity> beforeDate(LocalDate beforeDate) {
        return (root, query, criteriaBuilder) ->
                (beforeDate == null) ? null : criteriaBuilder.lessThanOrEqualTo(root.get("date"), beforeDate);
    }

    // Search theo keyword trong title hoặc description (không phân biệt hoa thường)
    public static Specification<EventEntity> keywordInTitleOrDescription(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.isEmpty()) return null;
            String pattern = "%" + keyword.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern)
            );
        };
    }
    public static Specification<EventEntity> distanceEvent(String longitude, String latitude, Integer distanceKm) {
        return (root, query, criteriaBuilder) -> {
            // Return null if any required param is missing
            if (longitude == null || latitude == null || distanceKm == null) {
                return null;
            }
            
            int precision;
            if (distanceKm < 10) precision = 5;
            else if (distanceKm < 50) precision = 4;
            else if (distanceKm < 150) precision = 3;
            else if (distanceKm < 600) precision = 2;
            else precision = 1;
            
            String geohash = GeoHash.encode(latitude, longitude, precision);
            if (geohash != null && !geohash.isEmpty()) {
                String pattern = geohash + "%";
                return criteriaBuilder.like(root.get("geohash"), pattern);
            }
            return null;
        };
    }
}
