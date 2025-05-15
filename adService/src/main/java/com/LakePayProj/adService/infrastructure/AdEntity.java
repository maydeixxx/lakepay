package com.LakePayProj.adService.infrastructure;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "ads")
public class AdEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;
    private String title;
    private String body;
    private String category;
    private BigDecimal price;
    private String login;
    private String password;
    private Integer countOfViews;
    private Integer quantity;
    private LocalDate dateOfPush;
    private Boolean sold;
}
