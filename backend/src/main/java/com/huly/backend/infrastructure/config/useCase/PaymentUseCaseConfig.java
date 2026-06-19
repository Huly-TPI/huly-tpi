package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.port.MercadoPagoPort;
import com.huly.backend.domain.repository.payment.PaymentEventRepository;
import com.huly.backend.domain.repository.payment.ProductRepository;
import com.huly.backend.domain.repository.user.UserPlanRepository;
import com.huly.backend.domain.service.payment.CoinService;
import com.huly.backend.domain.service.payment.PlanService;
import com.huly.backend.domain.useCase.payment.CreatePaymentPreferenceUseCase;
import com.huly.backend.domain.useCase.payment.HandleWebhookUseCase;
import com.huly.backend.domain.useCase.payment.ListPlansUseCase;
import com.huly.backend.domain.useCase.payment.ListProductsUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentUseCaseConfig {

    @Bean
    public CreatePaymentPreferenceUseCase createPaymentPreferenceUseCase(
            ProductRepository productRepository,
            MercadoPagoPort mercadoPagoPort,
            PaymentEventRepository paymentEventRepository,
            UserPlanRepository userPlanRepository) {
        return new CreatePaymentPreferenceUseCase(productRepository, mercadoPagoPort, paymentEventRepository, userPlanRepository);
    }

    @Bean
    public ListProductsUseCase listProductsUseCase(ProductRepository productRepository) {
        return new ListProductsUseCase(productRepository);
    }

    @Bean
    public ListPlansUseCase listPlansUseCase(ProductRepository productRepository) {
        return new ListPlansUseCase(productRepository);
    }

    @Bean
    public HandleWebhookUseCase handleWebhookUseCase(
            PaymentEventRepository paymentEventRepository,
            MercadoPagoPort mercadoPagoPort,
            CoinService coinDomainService,
            PlanService planDomainService) {
        return new HandleWebhookUseCase(paymentEventRepository, mercadoPagoPort, coinDomainService, planDomainService);
    }


}
