package com.huly.backend.infrastructure.repository.entity;
import com.huly.backend.domain.model.enums.ItemCategory;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "store_item")
public class StoreItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    
    private String name;
    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ItemCategory category;
    
    @Column(name = "asset_key", nullable = false, unique = true, length = 100)
    private String assetKey;
    
    @Column(name = "price_coins", nullable = false)
    private int priceCoins;
    
    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "premium_only", nullable = false)
    private boolean premiumOnly;
}
