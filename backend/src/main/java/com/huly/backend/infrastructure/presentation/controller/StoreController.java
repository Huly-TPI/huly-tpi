package com.huly.backend.infrastructure.presentation.controller;
import com.huly.backend.domain.model.StoreItem;
import com.huly.backend.domain.model.UserStoreItem;
import com.huly.backend.domain.useCase.store.BuyStoreItemUseCase;
import com.huly.backend.domain.useCase.store.EquipStoreItemUseCase;
import com.huly.backend.domain.useCase.store.GetUserInventoryUseCase;
import com.huly.backend.domain.useCase.store.ListStoreItemsUseCase;
import com.huly.backend.infrastructure.presentation.dto.store.InventoryItemResponse;
import com.huly.backend.infrastructure.presentation.dto.store.StoreItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/store")
@RequiredArgsConstructor
public class StoreController {

    private final ListStoreItemsUseCase listStoreItemsUseCase;
    private final GetUserInventoryUseCase getUserInventoryUseCase;
    private final BuyStoreItemUseCase buyStoreItemUseCase;
    private final EquipStoreItemUseCase equipStoreItemUseCase;

    @GetMapping("/items")
    public ResponseEntity<List<StoreItemResponse>> getItems() {
        List<StoreItemResponse> response = listStoreItemsUseCase.execute().stream().map(this::toItemResponse).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/inventory")
    public ResponseEntity<List<InventoryItemResponse>> getMyInventory(@AuthenticationPrincipal UserDetails userDetails) {
        List<InventoryItemResponse> response = getUserInventoryUseCase.execute(Long.parseLong(userDetails.getUsername())).stream()
        .map(this::toInventoryResponse).toList();
        return ResponseEntity.ok(response);
    }


    @PostMapping("/items/{id}/buy")
    public ResponseEntity<Void> buyItem(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        buyStoreItemUseCase.execute(Long.parseLong(userDetails.getUsername()), id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/items/{id}/equip")
    public ResponseEntity<Void> equipItem(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        equipStoreItemUseCase.execute(Long.parseLong(userDetails.getUsername()), id);
        return ResponseEntity.ok().build();
    }

    private StoreItemResponse toItemResponse(StoreItem item) {
        return new StoreItemResponse( 
                item.getId(),
                item.getName(), 
                item.getDescription(),
                item.getCategory().name(),
                item.getAssetKey(),
                item.getPriceCoins()
        );
    }

    private InventoryItemResponse toInventoryResponse(UserStoreItem owned) {
        return new InventoryItemResponse(
                owned.getStoreItem().getId(),
                owned.getStoreItem().getName(),
                owned.getStoreItem().getCategory().name(),
                owned.getStoreItem().getAssetKey(),
                owned.isEquipped()
        );
    }
    
}
