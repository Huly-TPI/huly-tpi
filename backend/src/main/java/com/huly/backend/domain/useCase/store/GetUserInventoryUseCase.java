package com.huly.backend.domain.useCase.store;
import com.huly.backend.domain.model.UserStoreItem;
import com.huly.backend.domain.repository.UserStoreItemRepository;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RequiredArgsConstructor
public class GetUserInventoryUseCase {

    private final UserStoreItemRepository userStoreItemRepository;
    
    public List<UserStoreItem> execute(Long userId) {
        return userStoreItemRepository.findAllByUserId(userId);
    }
    
}
