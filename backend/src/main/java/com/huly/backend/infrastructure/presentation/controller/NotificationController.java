package com.huly.backend.infrastructure.presentation.controller;
import com.huly.backend.domain.useCase.pushNotification.UnsubscribeFromEmailsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final UnsubscribeFromEmailsUseCase unsubscribeFromEmailsUseCase;

    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @GetMapping("/unsubscribe")
    public ResponseEntity<Void> unsubscribe(@RequestParam("token") String token) {
        boolean ok = unsubscribeFromEmailsUseCase.execute(token);
        String status = ok ? "ok" : "error";
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(frontendUrl + "/unsubscribe?status=" + status)).build();
        
    }
    
}
