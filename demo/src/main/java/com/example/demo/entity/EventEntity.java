package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "events",
        indexes = {
                @Index(name = "idx_eventType", columnList = "eventType"),
                @Index(name = "idx_date_location", columnList = "date, location")
        })
public class EventEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;
    private String title;
    private String description;
    private String location;
    private String longitude;
    private String latitude;
    private String geohash;
    private LocalDate date;
    private LocalTime time;
    private String eventType;
    private String tag;
    private String subTitle;
    private String eventStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    @JsonIgnore
    private UserEntity user;

    @OneToMany(mappedBy ="event", cascade = CascadeType.ALL)
    private List<SeatEntity> seats;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<TicketEntity> tickets;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "event")
    private List<PictureEntity> pictureUrls;
}
