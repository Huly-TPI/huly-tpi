package com.huly.backend.infrastructure.repository.entity;

import com.huly.backend.domain.model.enums.ProductType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "product")
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "coins_amount", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer coinsAmount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'COIN_PACK'")
    private ProductType type = ProductType.COIN_PACK;

    @Column(name = "plan_code", length = 50)
    private String planCode;

    @Column(name = "chat_daily_limit")
    private Integer chatDailyLimit;

    @Column(name = "audio_daily_limit")
    private Integer audioDailyLimit;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
