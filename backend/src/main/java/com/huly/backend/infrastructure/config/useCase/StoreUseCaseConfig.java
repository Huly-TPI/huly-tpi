package com.huly.backend.infrastructure.config.useCase;
import com.huly.backend.domain.repository.StoreItemRepository;
import com.huly.backend.domain.repository.UserStoreItemRepository;
import com.huly.backend.domain.service.payment.CoinService;
import com.huly.backend.domain.useCase.store.BuyStoreItemUseCase;
import com.huly.backend.domain.useCase.store.EquipStoreItemUseCase;
import com.huly.backend.domain.useCase.store.GetUserInventoryUseCase;
import com.huly.backend.domain.useCase.store.ListStoreItemsUseCase;
import com.mercadopago.resources.user.User;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StoreUseCaseConfig {

    @Bean
    public ListStoreItemsUseCase listStoreItemsUseCase(StoreItemRepository storeItemRepository) {
        return new ListStoreItemsUseCase(storeItemRepository);
    }

    @Bean
    public GetUserInventoryUseCase getUserInventoryUseCase(UserStoreItemRepository userStoreItemRepository) {
        return new GetUserInventoryUseCase(userStoreItemRepository);
    }

    @Bean
    public BuyStoreItemUseCase buyStoreItemUseCase(StoreItemRepository storeItemRepository,
                                                   UserStoreItemRepository userStoreItemRepository,
                                                   CoinService coinService) {
        return new BuyStoreItemUseCase(storeItemRepository, userStoreItemRepository, coinService);
    }

    @Bean 
    public EquipStoreItemUseCase equipStoreItemUseCase(StoreItemRepository storeItemRepository, UserStoreItemRepository userStoreItemRepository) {
        return new EquipStoreItemUseCase(storeItemRepository, userStoreItemRepository);
    }
    
}
