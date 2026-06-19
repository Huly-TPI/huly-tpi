package com.huly.backend.domain.model;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStoreItem {

    private Long id;
    private Long userId;
    private StoreItem storeItem;
    private boolean equipped;
    private Instant acquiredAt;
    
}
