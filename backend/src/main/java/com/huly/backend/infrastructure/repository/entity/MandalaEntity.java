package com.huly.backend.infrastructure.repository.entity;

import com.huly.backend.domain.model.enums.MandalaAccessType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "mandala")
public class MandalaEntity {

    @Id
    @Column(length = 100)
    private String id;

    @Column(nullable = false)
    private String title;

    @Column
    private String description;

    @Column(name = "asset_key", nullable = false, unique = true, length = 100)
    private String assetKey;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private boolean active;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_type", nullable = false, length = 20)
    private MandalaAccessType accessType;

    @Column(name = "price_coins")
    private Integer priceCoins;
}
