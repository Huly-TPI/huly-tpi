package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.payment.PaymentPreferenceResult;
import com.huly.backend.domain.useCase.payment.CreatePaymentPreferenceUseCase;
import com.huly.backend.domain.useCase.payment.ListPlansUseCase;
import com.huly.backend.domain.useCase.payment.ListProductsUseCase;
import com.huly.backend.infrastructure.presentation.dto.payment.CreatePreferenceResponse;
import com.huly.backend.infrastructure.presentation.dto.payment.PlanResponse;
import com.huly.backend.infrastructure.presentation.dto.payment.ProductResponse;
import com.huly.backend.domain.useCase.payment.CreateStoreItemPreferenceUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final ListProductsUseCase listProductsUseCase;
    private final ListPlansUseCase listPlansUseCase;
    private final CreatePaymentPreferenceUseCase createPaymentPreferenceUseCase;
    private final CreateStoreItemPreferenceUseCase createStoreItemPreferenceUseCase;

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

    @GetMapping("/plans")
    public ResponseEntity<List<PlanResponse>> getPlans() {
        List<PlanResponse> plans = listPlansUseCase.execute().stream()
                .map(p -> new PlanResponse(
                        String.valueOf(p.getId()),
                        p.getName(),
                        p.getDescription(),
                        p.getPrice(),
                        p.getCoinsAmount(),
                        p.getPlanCode()))
                .toList();
        return ResponseEntity.ok(plans);
    }

    @PostMapping("/preference/{productId}")
    public ResponseEntity<CreatePreferenceResponse> createPreference(@PathVariable Long productId,
            @AuthenticationPrincipal UserDetails principal) {
        Long userId = Long.parseLong(principal.getUsername());
        PaymentPreferenceResult result = createPaymentPreferenceUseCase.execute(productId, userId);
        return ResponseEntity.ok(new CreatePreferenceResponse(result.getId(), result.getInitPoint()));
    }

    @PostMapping("/store-preference/{storeItemId}")
    public ResponseEntity<CreatePreferenceResponse> createStoreItemPreference(
            @PathVariable Long storeItemId, @AuthenticationPrincipal UserDetails principal) {
        Long userId = Long.parseLong(principal.getUsername());
        PaymentPreferenceResult result = createStoreItemPreferenceUseCase.execute(storeItemId, userId);
        return ResponseEntity.ok(new CreatePreferenceResponse(result.getId(), result.getInitPoint()));
    }

}
