package com.example.demo.entity;

import com.example.demo.entity.enums.Role;
import com.example.demo.entity.enums.Status;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_email", columnList = "email", unique = true),
                @Index(name = "idx_username", columnList = "username"),
                @Index(name = "idx_status_createdAt", columnList = "status, createdAt")
        }
)
@Builder
        
public class UserEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId")
    private Long userId;

    @Column(nullable = false, length = 50, name = "username")
    private String username;

    @Column(nullable = false, unique = true, name = "email")
    private String email;

    @JsonIgnore
    @Column(name = "password")
    private String password;


    @Enumerated(EnumType.STRING)
    @Column(nullable = true, columnDefinition = "varchar(20) default 'USER'", name = "role")
    private Role role;

    @Column(nullable = true, name = "phone_number")
    private String phoneNumber;

    @Column(nullable = true, name = "address")
    private String address;

    @Column(nullable = true, name = "full_name")
    private String fullName;

    @Column(nullable = true, name = "date_of_birth")
    private Date dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, columnDefinition = "varchar(20) default 'ACTIVE'", name = "status")
    private Status status;

    @Column(nullable = false, columnDefinition = "boolean default true", name = "has_password")
    private boolean hasPassword;

    @Column(nullable = true, name = "provider")
    private String provider;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventEntity> events;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderEntity> orders;

}
