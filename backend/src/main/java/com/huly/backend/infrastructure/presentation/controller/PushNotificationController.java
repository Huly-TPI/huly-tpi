package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.dto.pushNotification.UpdateNotificationHourRequest;
import com.huly.backend.domain.useCase.pushNotification.DeletePushSubscriptionUseCase;
import com.huly.backend.domain.useCase.pushNotification.GetPushSubscriptionStatusUseCase;
import com.huly.backend.domain.useCase.pushNotification.SavePushSubscriptionUseCase;
import com.huly.backend.infrastructure.presentation.dto.pushNotification.PushSubscriptionStatusResponse;
import com.huly.backend.infrastructure.presentation.dto.pushNotification.SubscribeRequest;
import com.huly.backend.infrastructure.presentation.dto.pushNotification.UnsubscribeRequest;
import com.huly.backend.infrastructure.presentation.mapper.pushNotification.PushNotificationPresentationMapper;
import com.huly.backend.domain.useCase.pushNotification.UpdateNotificationHourUseCase;
import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pushNotification")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class PushNotificationController {

    private final SavePushSubscriptionUseCase savePushSubscriptionUseCase;
    private final DeletePushSubscriptionUseCase deletePushSubscriptionUseCase;
    private final GetPushSubscriptionStatusUseCase getPushSubscriptionStatusUseCase;
    private final PushNotificationPresentationMapper pushNotificationPresentationMapper;
    private final UpdateNotificationHourUseCase updateNotificationHourUseCase;

    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribe(@RequestBody SubscribeRequest request) {
        savePushSubscriptionUseCase.execute(pushNotificationPresentationMapper.toSaveRequest(request));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/unsubscribe")
    public ResponseEntity<Void> unsubscribe(@RequestBody UnsubscribeRequest request) {
        deletePushSubscriptionUseCase.execute(pushNotificationPresentationMapper.toDeleteRequest(request));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status")
    public ResponseEntity<PushSubscriptionStatusResponse> status(@AuthenticationPrincipal UserDetails principal) {
        Long userId = Long.parseLong(principal.getUsername());
        PushSubscriptionStatusResponse response = pushNotificationPresentationMapper.toStatusResponse(
                getPushSubscriptionStatusUseCase.execute(pushNotificationPresentationMapper.toStatusRequest(userId)));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/hour")
    public ResponseEntity<Void> updateHour(@AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody UpdateNotificationHourRequest request) {
        Long userId = Long.parseLong(principal.getUsername());
        updateNotificationHourUseCase.execute(userId, request.hour());
        return ResponseEntity.noContent().build();
    }

}
