package com.LakePayProj.userService.entity;

import com.LakePayProj.userService.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Long telegramId;

    @Column(unique = true, nullable = false)
    private String username;

    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime creationDate;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserCredential credential;
}
