package com.huly.backend.infrastructure.presentation.controller;
import com.huly.backend.domain.useCase.pushNotification.DeletePushSubscriptionUseCase;
import com.huly.backend.domain.useCase.pushNotification.SavePushSubscriptionUseCase;
import com.huly.backend.infrastructure.presentation.dto.pushNotification.SubscribeRequest;
import com.huly.backend.infrastructure.presentation.dto.pushNotification.UnsubscribeRequest;
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

    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribe(@RequestBody SubscribeRequest request) {
        savePushSubscriptionUseCase.execute(request.getUserId(), request.getEndpoint(), request.getP256dh(), request.getAuth());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/unsubscribe")
    public ResponseEntity<Void> unsubscribe(@RequestBody UnsubscribeRequest request) {
        deletePushSubscriptionUseCase.execute(request.getEndpoint());
        return ResponseEntity.noContent().build();
    }


    
}
