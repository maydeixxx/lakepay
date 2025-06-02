package com.lakepayProj.userService.infrastructure;

import com.lakepayProj.userService.domain.valueObject.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Entity
@Data
@Builder
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;
    @Column(unique = true)
    private String username;
    private List<String> subscriptions;
    private Long tgId;
    private Long chatId;
    private String urlPhoto;
    private LocalDate dateOfReg;
    private BigDecimal balance;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )

    private Collection<Role> roles;
}
