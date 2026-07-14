package com.huly.backend.domain.useCase.payment;

import com.huly.backend.domain.model.payment.Product;
import com.huly.backend.domain.model.enums.ProductType;
import com.huly.backend.domain.repository.payment.ProductRepository;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RequiredArgsConstructor
public class ListProductsUseCase {

    private final ProductRepository productRepository;

    /** Solo packs de monedas; los planes se listan aparte (ListPlansUseCase). */
    public List<Product> execute() {
        return productRepository.findByTypeAndActive(ProductType.COIN_PACK, true);
    }
}
