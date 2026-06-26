package com.huly.backend.infrastructure.config.useCase;
import com.huly.backend.domain.mapper.store.BuyStoreItemMapper;
import com.huly.backend.domain.mapper.store.EquipStoreItemMapper;
import com.huly.backend.domain.mapper.store.GetUserInventoryMapper;
import com.huly.backend.domain.mapper.store.ListStoreItemsMapper;
import com.huly.backend.domain.mapper.store.UnequipStoreItemMapper;
import com.huly.backend.domain.repository.StoreItemRepository;
import com.huly.backend.domain.repository.UserStoreItemRepository;
import com.huly.backend.domain.repository.user.UserPlanRepository;
import com.huly.backend.domain.service.payment.CoinService;
import com.huly.backend.domain.useCase.store.BuyStoreItemUseCase;
import com.huly.backend.domain.useCase.store.EquipStoreItemUseCase;
import com.huly.backend.domain.useCase.store.GetUserInventoryUseCase;
import com.huly.backend.domain.useCase.store.ListStoreItemsUseCase;
import com.huly.backend.domain.useCase.store.UnequipStoreItemUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StoreUseCaseConfig {

    @Bean
    public ListStoreItemsMapper listStoreItemsMapper() {
        return new ListStoreItemsMapper();
    }

    @Bean
    public GetUserInventoryMapper getUserInventoryMapper() {
        return new GetUserInventoryMapper();
    }

    @Bean
    public EquipStoreItemMapper equipStoreItemMapper() {
        return new EquipStoreItemMapper();
    }

    @Bean
    public UnequipStoreItemMapper unequipStoreItemMapper() {
        return new UnequipStoreItemMapper();
    }

    @Bean
    public BuyStoreItemMapper buyStoreItemMapper() {
        return new BuyStoreItemMapper();
    }

    @Bean
    public ListStoreItemsUseCase listStoreItemsUseCase(StoreItemRepository storeItemRepository,
                                                       ListStoreItemsMapper listStoreItemsMapper) {
        return new ListStoreItemsUseCase(storeItemRepository, listStoreItemsMapper);
    }

    @Bean
    public GetUserInventoryUseCase getUserInventoryUseCase(UserStoreItemRepository userStoreItemRepository,
                                                           GetUserInventoryMapper getUserInventoryMapper) {
        return new GetUserInventoryUseCase(userStoreItemRepository, getUserInventoryMapper);
    }

    @Bean
    public BuyStoreItemUseCase buyStoreItemUseCase(StoreItemRepository storeItemRepository,
                                                   UserStoreItemRepository userStoreItemRepository,
                                                   CoinService coinService, UserPlanRepository userPlanRepository,
                                                   BuyStoreItemMapper buyStoreItemMapper) {
        return new BuyStoreItemUseCase(storeItemRepository, userStoreItemRepository, coinService, userPlanRepository, buyStoreItemMapper);
    }

    @Bean
    public EquipStoreItemUseCase equipStoreItemUseCase(StoreItemRepository storeItemRepository,
                                                       UserStoreItemRepository userStoreItemRepository,
                                                       EquipStoreItemMapper equipStoreItemMapper) {
        return new EquipStoreItemUseCase(storeItemRepository, userStoreItemRepository, equipStoreItemMapper);
    }

    @Bean
    public UnequipStoreItemUseCase unequipStoreItemUseCase(UserStoreItemRepository userStoreItemRepository,
                                                           UnequipStoreItemMapper unequipStoreItemMapper) {
        return new UnequipStoreItemUseCase(userStoreItemRepository, unequipStoreItemMapper);
    }

}
