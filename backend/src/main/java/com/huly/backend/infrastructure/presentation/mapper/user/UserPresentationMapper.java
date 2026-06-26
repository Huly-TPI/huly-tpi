package com.huly.backend.infrastructure.presentation.mapper.user;

import com.huly.backend.domain.dto.user.GetCurrentMembershipRequest;
import com.huly.backend.domain.dto.user.GetCurrentMembershipResponse;
import com.huly.backend.domain.dto.user.GetUserCoinsRequest;
import com.huly.backend.domain.dto.user.GetUserCoinsResponse;
import com.huly.backend.infrastructure.presentation.dto.user.CoinsResponse;
import com.huly.backend.infrastructure.presentation.dto.user.MembershipResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper de presentacion para el feature de usuario:
 * traduce entre los DTOs web y los DTOs de dominio.
 */
@Component
public class UserPresentationMapper {

    public GetUserCoinsRequest toCoinsRequest(Long userId) {
        return new GetUserCoinsRequest(userId);
    }

    public CoinsResponse toCoinsResponse(GetUserCoinsResponse response) {
        return new CoinsResponse(response.coins());
    }

    public GetCurrentMembershipRequest toMembershipRequest(Long userId) {
        return new GetCurrentMembershipRequest(userId);
    }

    public MembershipResponse toMembershipResponse(GetCurrentMembershipResponse response) {
        if (!response.active()) {
            return MembershipResponse.inactive();
        }
        return new MembershipResponse(
                true,
                response.planCode(),
                response.productId() != null ? response.productId().toString() : null,
                response.expiresAt()
        );
    }
}
