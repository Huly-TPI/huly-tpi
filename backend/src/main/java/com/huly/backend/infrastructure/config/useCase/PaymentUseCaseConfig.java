package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.port.MercadoPagoPort;
import com.huly.backend.domain.repository.PaymentEventRepository;
import com.huly.backend.domain.repository.ProductRepository;
import com.huly.backend.domain.useCase.payment.CreatePaymentPreferenceUseCase;
import com.huly.backend.domain.useCase.payment.ListProductsUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentUseCaseConfig {

    @Bean
    public CreatePaymentPreferenceUseCase createPaymentPreferenceUseCase(
            ProductRepository productRepository,
            MercadoPagoPort mercadoPagoPort,
            PaymentEventRepository paymentEventRepository) {
        return new CreatePaymentPreferenceUseCase(productRepository, mercadoPagoPort, paymentEventRepository);
    }

    @Bean
    public ListProductsUseCase listProductsUseCase(ProductRepository productRepository) {
        return new ListProductsUseCase(productRepository);
    }
}
