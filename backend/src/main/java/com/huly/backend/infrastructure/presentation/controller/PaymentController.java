package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.dto.payment.PaymentPreferenceResult;
import com.huly.backend.domain.useCase.payment.CreatePaymentPreferenceUseCase;
import com.huly.backend.domain.useCase.payment.ListProductsUseCase;
import com.huly.backend.infrastructure.presentation.dto.payment.CreatePreferenceResponse;
import com.huly.backend.infrastructure.presentation.dto.payment.ProductResponse;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final ListProductsUseCase listProductsUseCase;
    private final CreatePaymentPreferenceUseCase createPaymentPreferenceUseCase;
    private final AppUserRepository appUserRepository;

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getProducts() {
        List<ProductResponse> products = listProductsUseCase.execute().stream()
                .map(p -> new ProductResponse(
                        String.valueOf(p.getId()),
                        p.getName(),
                        p.getDescription(),
                        p.getPrice(),
                        p.getCoinsAmount()))
                .toList();
        return ResponseEntity.ok(products);
    }

    @PostMapping("/preference/{productId}")
    public ResponseEntity<CreatePreferenceResponse> createPreference(@PathVariable Long productId) {
        Long userId = resolveUserId();
        PaymentPreferenceResult result = createPaymentPreferenceUseCase.execute(productId, userId);
        return ResponseEntity.ok(new CreatePreferenceResponse(result.getId(), result.getInitPoint()));
    }

    @GetMapping("/me/coins")
    public ResponseEntity<Map<String, Integer>> getMyCoins() {
        Long userId = resolveUserId();
        int coins = appUserRepository.findCoinsById(userId).orElse(0);
        return ResponseEntity.ok(Map.of("coins", coins));
    }

    private Long resolveUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return appUserRepository.findByEmail(email)
                .orElseThrow(() -> new com.huly.backend.infrastructure.presentation.exception.NotFoundException("Usuario no encontrado"))
                .getId();
    }
}
