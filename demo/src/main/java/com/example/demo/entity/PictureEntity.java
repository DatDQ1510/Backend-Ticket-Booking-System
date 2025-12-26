package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import net.minidev.json.annotate.JsonIgnore;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(
        name = "pictures",
        indexes = {
                @Index(name = "idx_eventId", columnList = "eventId")
        }
)
public class PictureEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pictureId;

    private String pictureUrl;

    private String publicId;

    private boolean isMain;
    @ManyToOne
    @JoinColumn(name = "eventId", nullable = true)
    @JsonIgnore
    private EventEntity event;
}
