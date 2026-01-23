package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import net.minidev.json.annotate.JsonIgnore;

import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(
    name = "user_activity_log",
    indexes = {
        @Index(name = "idx_user_activity_user_id", columnList = "user_id"),
        @Index(name = "idx_user_activity_timestamp", columnList = "activity_timestamp")
    }
)
@Builder
public class UserActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "active_user_id")
    private Long activeUserId;

    @Column(name = "activity_type")
    private String activityType;

    @Column(name = "activity_timestamp")
    private LocalDateTime activityTimestamp;

    // Sửa name="userId" thành "user_id" để khớp với DB
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private UserEntity user;
}
