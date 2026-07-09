package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.dto.payment.CreateProductRequest;
import com.huly.backend.domain.dto.payment.UpdateProductRequest;
import com.huly.backend.domain.model.enums.ProductType;
import com.huly.backend.domain.model.payment.Product;
import com.huly.backend.domain.useCase.payment.CreateProductUseCase;
import com.huly.backend.domain.useCase.payment.ListAdminProductsUseCase;
import com.huly.backend.domain.useCase.payment.SetProductActiveUseCase;
import com.huly.backend.domain.useCase.payment.UpdateProductUseCase;
import com.huly.backend.infrastructure.presentation.dto.payment.AdminProductResponse;
import com.huly.backend.infrastructure.presentation.dto.payment.ProductWebRequest;
import com.huly.backend.infrastructure.presentation.dto.payment.SetActiveWebRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class ProductAdminController {

    private final ListAdminProductsUseCase listAdminProductsUseCase;
    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final SetProductActiveUseCase setProductActiveUseCase;

    @GetMapping
    public ResponseEntity<List<AdminProductResponse>> list(
            @RequestParam(defaultValue = "COIN_PACK") ProductType type) {
        return ResponseEntity.ok(listAdminProductsUseCase.execute(type).stream().map(this::toResponse).toList());
    }

    @PostMapping
    public ResponseEntity<AdminProductResponse> create(@Valid @RequestBody ProductWebRequest req) {
        Product created = createProductUseCase.execute(new CreateProductRequest(
                req.name(), req.description(), req.price(), req.coinsAmount(),
                req.type(), req.planCode(), req.chatDailyLimit(), req.audioDailyLimit()));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminProductResponse> update(@PathVariable Long id,
            @Valid @RequestBody ProductWebRequest req) {
        Product updated = updateProductUseCase.execute(new UpdateProductRequest(
                id, req.name(), req.description(), req.price(), req.coinsAmount(),
                req.type(), req.planCode(), req.chatDailyLimit(), req.audioDailyLimit()));
        return ResponseEntity.ok(toResponse(updated));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<AdminProductResponse> setActive(@PathVariable Long id,
            @RequestBody SetActiveWebRequest req) {
        return ResponseEntity.ok(toResponse(setProductActiveUseCase.execute(id, req.active())));
    }

    private AdminProductResponse toResponse(Product p) {
        return new AdminProductResponse(p.getId(), p.getName(), p.getDescription(), p.getPrice(),
                p.getCoinsAmount(), p.getType().name(), p.getPlanCode(), p.getChatDailyLimit(),
                p.getAudioDailyLimit(), p.isActive());
    }
}