package com.huly.backend.domain.model.payment;

import com.huly.backend.domain.model.enums.ProductType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class Product {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer coinsAmount;
    private ProductType type;
    private String planCode;
    private Integer chatDailyLimit;
}
