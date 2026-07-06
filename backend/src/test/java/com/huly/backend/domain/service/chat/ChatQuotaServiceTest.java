package com.huly.backend.domain.service.chat;

import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.model.enums.ProductType;
import com.huly.backend.domain.model.payment.Product;
import com.huly.backend.domain.model.user.UserPlan;
import com.huly.backend.domain.repository.chat.ChatMessageRepository;
import com.huly.backend.domain.repository.payment.ProductRepository;
import com.huly.backend.domain.repository.user.UserPlanRepository;
import com.huly.backend.domain.service.chat.ChatQuotaService.RemainingAudioQuota;
import com.huly.backend.domain.service.chat.ChatQuotaService.RemainingQuota;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatQuotaServiceTest {

    private static final Long USER_ID = 10L;
    private static final Long PRODUCT_ID = 5L;

    @Mock
    private UserPlanRepository userPlanRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ChatMessageRepository chatMessageRepository;

    @InjectMocks
    private ChatQuotaService service;

    // ── assertWithinLimit: usuario free (sin plan) ───────────────────────────

    @Test
    @DisplayName("No lanza cuando el usuario free no alcanzó el tope diario")
    void assertWithinLimitNoLanzaCuandoFreeBajoElTope() {
        givenNoActivePlan();
        givenChatMessagesUsed(4L);

        thenAssertWithinLimitDoesNotThrow();
        thenProductRepositoryNotUsed();
    }

    @Test
    @DisplayName("Lanza con mensaje del plan gratuito cuando el usuario free alcanzó el tope")
    void assertWithinLimitLanzaCuandoFreeAlcanzoElTope() {
        givenNoActivePlan();
        givenChatMessagesUsed(5L);

        thenAssertWithinLimitThrowsFreePlanMessage();
    }

    // ── assertWithinLimit: usuario con plan activo ───────────────────────────

    @Test
    @DisplayName("No lanza ni cuenta mensajes cuando el plan tiene tope de chat nulo (ilimitado)")
    void assertWithinLimitNoLanzaNiCuentaCuandoPlanTieneTopeNulo() {
        givenActivePlan(PRODUCT_ID);
        givenProductWithChatLimit(null);

        thenAssertWithinLimitDoesNotThrow();
        thenChatMessagesNotCounted();
    }

    @Test
    @DisplayName("No lanza cuando el usuario con plan no alcanzó el tope del plan")
    void assertWithinLimitNoLanzaCuandoPlanBajoElTope() {
        givenActivePlan(PRODUCT_ID);
        givenProductWithChatLimit(20);
        givenChatMessagesUsed(19L);

        thenAssertWithinLimitDoesNotThrow();
    }

    @Test
    @DisplayName("Lanza con mensaje del plan cuando el usuario con plan alcanzó el tope del plan")
    void assertWithinLimitLanzaCuandoPlanAlcanzoElTope() {
        givenActivePlan(PRODUCT_ID);
        givenProductWithChatLimit(20);
        givenChatMessagesUsed(20L);

        thenAssertWithinLimitThrowsPaidPlanMessage();
    }

    @Test
    @DisplayName("Trata un plan vencido como usuario free y lanza al alcanzar el tope free")
    void assertWithinLimitTrataPlanVencidoComoFree() {
        givenExpiredPlan();
        givenChatMessagesUsed(5L);

        thenAssertWithinLimitThrowsBusinessRule();
        thenProductRepositoryNotUsed();
    }

    @Test
    @DisplayName("No lanza ni consulta el producto cuando el plan activo no tiene productId")
    void assertWithinLimitNoLanzaCuandoPlanActivoSinProducto() {
        givenActivePlan(null);

        thenAssertWithinLimitDoesNotThrow();
        thenProductRepositoryNotUsed();
    }

    @Test
    @DisplayName("No lanza ni cuenta mensajes cuando no se encuentra el producto del plan")
    void assertWithinLimitNoLanzaCuandoNoSeEncuentraElProducto() {
        givenActivePlan(PRODUCT_ID);
        givenProductNotFound();

        thenAssertWithinLimitDoesNotThrow();
        thenChatMessagesNotCounted();
    }

    // ── getRemainingQuota ────────────────────────────────────────────────────

    @Test
    @DisplayName("Informa los mensajes restantes cuando el usuario free no alcanzó el tope")
    void getRemainingQuotaInformaRestantesCuandoFreeBajoElTope() {
        givenNoActivePlan();
        givenChatMessagesUsed(2L);

        RemainingQuota result = getRemainingQuota();

        thenRemainingMessagesAre(result, 3);
        thenQuotaHasNoLimitMessage(result);
    }

    @Test
    @DisplayName("Informa cero y mensaje del plan gratuito cuando el usuario free alcanzó el tope")
    void getRemainingQuotaInformaCeroYMensajeCuandoFreeAlcanzoElTope() {
        givenNoActivePlan();
        givenChatMessagesUsed(5L);

        RemainingQuota result = getRemainingQuota();

        thenRemainingMessagesAre(result, 0);
        thenQuotaLimitMessageContains(result, "plan gratuito");
    }

    @Test
    @DisplayName("Informa restante nulo y sin mensaje cuando el plan es ilimitado")
    void getRemainingQuotaInformaNuloCuandoPlanEsIlimitado() {
        givenActivePlan(PRODUCT_ID);
        givenProductWithChatLimit(null);

        RemainingQuota result = getRemainingQuota();

        thenRemainingQuotaIsUnlimited(result);
    }

    @Test
    @DisplayName("Informa cero y mensaje del plan cuando el usuario con plan superó el tope")
    void getRemainingQuotaInformaCeroYMensajeCuandoPlanSuperoElTope() {
        givenActivePlan(PRODUCT_ID);
        givenProductWithChatLimit(20);
        givenChatMessagesUsed(25L);

        RemainingQuota result = getRemainingQuota();

        thenRemainingMessagesAre(result, 0);
        thenQuotaLimitMessageContains(result, "de tu plan");
    }

    // ── assertWithinAudioLimit ────────────────────────────────────────────────

    @Test
    @DisplayName("No lanza cuando el usuario con plan no alcanzó el tope de audios")
    void assertWithinAudioLimitNoLanzaCuandoBajoElTope() {
        givenActivePlan(PRODUCT_ID);
        givenProductWithAudioLimit(10);
        givenAudioMessagesUsed(9L);

        thenAssertWithinAudioLimitDoesNotThrow();
    }

    @Test
    @DisplayName("Lanza con mensaje de audios cuando el usuario con plan alcanzó el tope")
    void assertWithinAudioLimitLanzaCuandoAlcanzoElTope() {
        givenActivePlan(PRODUCT_ID);
        givenProductWithAudioLimit(10);
        givenAudioMessagesUsed(10L);

        thenAssertWithinAudioLimitThrows();
    }

    @Test
    @DisplayName("No lanza ni cuenta audios cuando el plan tiene tope de audios nulo (ilimitado)")
    void assertWithinAudioLimitNoLanzaCuandoTopeNulo() {
        givenActivePlan(PRODUCT_ID);
        givenProductWithAudioLimit(null);

        thenAssertWithinAudioLimitDoesNotThrow();
        thenChatMessagesNotCounted();
    }

    @Test
    @DisplayName("No lanza para el usuario free porque no tiene tope de audios")
    void assertWithinAudioLimitNoLanzaParaUsuarioFree() {
        givenNoActivePlan();

        thenAssertWithinAudioLimitDoesNotThrow();
        thenProductRepositoryNotUsed();
        thenChatMessagesNotCounted();
    }

    @Test
    @DisplayName("No lanza ni consulta el producto para audios cuando el plan activo no tiene productId")
    void assertWithinAudioLimitNoLanzaCuandoPlanActivoSinProducto() {
        givenActivePlan(null);

        thenAssertWithinAudioLimitDoesNotThrow();
        thenProductRepositoryNotUsed();
    }

    @Test
    @DisplayName("No lanza ni cuenta audios cuando no se encuentra el producto del plan")
    void assertWithinAudioLimitNoLanzaCuandoNoSeEncuentraElProducto() {
        givenActivePlan(PRODUCT_ID);
        givenProductNotFound();

        thenAssertWithinAudioLimitDoesNotThrow();
        thenChatMessagesNotCounted();
    }

    // ── getRemainingAudioQuota ────────────────────────────────────────────────

    @Test
    @DisplayName("Informa los audios restantes cuando el usuario con plan no alcanzó el tope")
    void getRemainingAudioQuotaInformaRestantesCuandoBajoElTope() {
        givenActivePlan(PRODUCT_ID);
        givenProductWithAudioLimit(10);
        givenAudioMessagesUsed(4L);

        RemainingAudioQuota result = getRemainingAudioQuota();

        thenRemainingAudiosAre(result, 6);
        thenAudioQuotaHasNoLimitMessage(result);
    }

    @Test
    @DisplayName("Informa cero y mensaje de audios cuando el usuario con plan superó el tope")
    void getRemainingAudioQuotaInformaCeroYMensajeCuandoSuperoElTope() {
        givenActivePlan(PRODUCT_ID);
        givenProductWithAudioLimit(10);
        givenAudioMessagesUsed(12L);

        RemainingAudioQuota result = getRemainingAudioQuota();

        thenRemainingAudiosAre(result, 0);
        thenAudioQuotaLimitMessageContains(result, "audios diarios");
    }

    @Test
    @DisplayName("Informa restante nulo y sin mensaje de audios cuando no hay tope (usuario free)")
    void getRemainingAudioQuotaInformaNuloCuandoNoHayTope() {
        givenNoActivePlan();

        RemainingAudioQuota result = getRemainingAudioQuota();

        thenRemainingAudioQuotaIsUnlimited(result);
    }

    // --- arrange ---
    private void givenNoActivePlan() {
        when(userPlanRepository.findByUser(USER_ID)).thenReturn(Optional.empty());
    }

    private void givenActivePlan(Long productId) {
        when(userPlanRepository.findByUser(USER_ID)).thenReturn(Optional.of(activePlan(productId)));
    }

    private void givenExpiredPlan() {
        when(userPlanRepository.findByUser(USER_ID)).thenReturn(Optional.of(expiredPlan()));
    }

    private void givenProductWithChatLimit(Integer chatDailyLimit) {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(productWithChatLimit(chatDailyLimit)));
    }

    private void givenProductWithAudioLimit(Integer audioDailyLimit) {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(productWithAudioLimit(audioDailyLimit)));
    }

    private void givenProductNotFound() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());
    }

    private void givenChatMessagesUsed(long count) {
        when(chatMessageRepository.countUserMessagesSince(eq(USER_ID), any())).thenReturn(count);
    }

    private void givenAudioMessagesUsed(long count) {
        when(chatMessageRepository.countUserAudioMessagesSince(eq(USER_ID), any())).thenReturn(count);
    }

    private UserPlan activePlan(Long productId) {
        return UserPlan.builder()
                .id(1L).userId(USER_ID).productId(productId).planCode("PREMIUM")
                .grantedAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
                .build();
    }

    private UserPlan expiredPlan() {
        return UserPlan.builder()
                .id(1L).userId(USER_ID).productId(PRODUCT_ID).planCode("PREMIUM")
                .grantedAt(Instant.now().minus(60, ChronoUnit.DAYS))
                .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .build();
    }

    private Product productWithChatLimit(Integer chatDailyLimit) {
        return Product.builder()
                .id(PRODUCT_ID).name("Plan PREMIUM").description("Suscripción")
                .type(ProductType.PLAN).planCode("PREMIUM").coinsAmount(0)
                .chatDailyLimit(chatDailyLimit)
                .build();
    }

    private Product productWithAudioLimit(Integer audioDailyLimit) {
        return Product.builder()
                .id(PRODUCT_ID).name("Plan PREMIUM").description("Suscripción")
                .type(ProductType.PLAN).planCode("PREMIUM").coinsAmount(0)
                .audioDailyLimit(audioDailyLimit)
                .build();
    }

    // --- act ---
    private RemainingQuota getRemainingQuota() {
        return service.getRemainingQuota(USER_ID);
    }

    private RemainingAudioQuota getRemainingAudioQuota() {
        return service.getRemainingAudioQuota(USER_ID);
    }

    // --- assert ---
    private void thenAssertWithinLimitDoesNotThrow() {
        assertThatCode(() -> service.assertWithinLimit(USER_ID)).doesNotThrowAnyException();
    }

    private void thenAssertWithinLimitThrowsFreePlanMessage() {
        assertThatThrownBy(() -> service.assertWithinLimit(USER_ID))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("plan gratuito");
    }

    private void thenAssertWithinLimitThrowsPaidPlanMessage() {
        assertThatThrownBy(() -> service.assertWithinLimit(USER_ID))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("de tu plan");
    }

    private void thenAssertWithinLimitThrowsBusinessRule() {
        assertThatThrownBy(() -> service.assertWithinLimit(USER_ID))
                .isInstanceOf(BusinessRuleException.class);
    }

    private void thenAssertWithinAudioLimitDoesNotThrow() {
        assertThatCode(() -> service.assertWithinAudioLimit(USER_ID)).doesNotThrowAnyException();
    }

    private void thenAssertWithinAudioLimitThrows() {
        assertThatThrownBy(() -> service.assertWithinAudioLimit(USER_ID))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("audios diarios");
    }

    private void thenRemainingMessagesAre(RemainingQuota result, Integer expected) {
        assertThat(result.remaining()).isEqualTo(expected);
    }

    private void thenQuotaHasNoLimitMessage(RemainingQuota result) {
        assertThat(result.limitMessage()).isNull();
    }

    private void thenQuotaLimitMessageContains(RemainingQuota result, String fragment) {
        assertThat(result.limitMessage()).contains(fragment);
    }

    private void thenRemainingQuotaIsUnlimited(RemainingQuota result) {
        assertThat(result.remaining()).isNull();
        assertThat(result.limitMessage()).isNull();
    }

    private void thenRemainingAudiosAre(RemainingAudioQuota result, Integer expected) {
        assertThat(result.remaining()).isEqualTo(expected);
    }

    private void thenAudioQuotaHasNoLimitMessage(RemainingAudioQuota result) {
        assertThat(result.limitMessage()).isNull();
    }

    private void thenAudioQuotaLimitMessageContains(RemainingAudioQuota result, String fragment) {
        assertThat(result.limitMessage()).contains(fragment);
    }

    private void thenRemainingAudioQuotaIsUnlimited(RemainingAudioQuota result) {
        assertThat(result.remaining()).isNull();
        assertThat(result.limitMessage()).isNull();
    }

    private void thenProductRepositoryNotUsed() {
        verifyNoInteractions(productRepository);
    }

    private void thenChatMessagesNotCounted() {
        verifyNoInteractions(chatMessageRepository);
    }
}
