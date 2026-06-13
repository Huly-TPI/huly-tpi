package com.huly.backend.infrastructure.config;

import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.Security;

@Configuration
public class VapidConfig {
    
    @Value("${vapid.public-key}")
    private String publicKey;
    
    @Value("${vapid.private-key}")
    private String privateKey;

        @Value("${vapid.subject")
    private String subject;
    
    
    @Bean
    public PushService pushService() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        return new PushService(publicKey, privateKey);
    }
}
