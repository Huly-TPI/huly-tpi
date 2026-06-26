package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.useCase.store.BuyStoreItemUseCase;
import com.huly.backend.domain.useCase.store.EquipStoreItemUseCase;
import com.huly.backend.domain.useCase.store.GetUserInventoryUseCase;
import com.huly.backend.domain.useCase.store.UnequipStoreItemUseCase;
import com.huly.backend.domain.useCase.store.ListStoreItemsUseCase;
import com.huly.backend.infrastructure.presentation.dto.store.InventoryItemResponse;
import com.huly.backend.infrastructure.presentation.dto.store.StoreItemResponse;
import com.huly.backend.infrastructure.presentation.mapper.store.StorePresentationMapper;
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
    private final UnequipStoreItemUseCase unequipStoreItemUseCase;
    private final StorePresentationMapper storePresentationMapper;

    @GetMapping("/items")
    public ResponseEntity<List<StoreItemResponse>> getItems() {
        return ResponseEntity.ok(storePresentationMapper.toStoreItemResponses(listStoreItemsUseCase.execute()));
    }

    @GetMapping("/inventory")
    public ResponseEntity<List<InventoryItemResponse>> getMyInventory(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(storePresentationMapper.toInventoryResponses(
                getUserInventoryUseCase.execute(storePresentationMapper.toInventoryRequest(userId))));
    }

    @PostMapping("/items/{id}/buy")
    public ResponseEntity<Void> buyItem(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        buyStoreItemUseCase.execute(storePresentationMapper.toBuyRequest(userId, id));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/items/{id}/equip")
    public ResponseEntity<Void> equipItem(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        equipStoreItemUseCase.execute(storePresentationMapper.toEquipRequest(userId, id));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/items/{id}/unequip")
    public ResponseEntity<Void> unequipItem(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        unequipStoreItemUseCase.execute(storePresentationMapper.toUnequipRequest(userId, id));
        return ResponseEntity.ok().build();
    }

}
