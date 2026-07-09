package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.payment.Product;
import com.huly.backend.domain.model.enums.ProductType;
import com.huly.backend.infrastructure.repository.entity.ProductEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IProductJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductRepositoryImplTest {

    @Mock
    private IProductJpaRepository jpaRepository;
    @InjectMocks
    private ProductRepositoryImpl repository;

    @Test
    @DisplayName("Mapea todas las entidades a dominio")
    void findAllShouldMapAllEntitiesToDomain() {
        givenAllProducts(coinPackEntity(), planEntity());

        List<Product> result = findAll();

        thenAllProductsMapped(result);
    }

    @Test
    @DisplayName("Devuelve lista vacía cuando no hay entidades")
    void findAllShouldReturnEmptyListWhenNoEntities() {
        givenAllProducts();

        List<Product> result = findAll();

        thenEmptyList(result);
    }

    @Test
    @DisplayName("Devuelve el producto mapeado por id cuando existe")
    void findByIdShouldReturnMappedProductWhenFound() {
        givenProductById(10L, planEntity());

        Optional<Product> result = findById(10L);

        thenProductFoundById(result);
    }

    @Test
    @DisplayName("Devuelve vacío al buscar por id cuando no existe")
    void findByIdShouldReturnEmptyWhenNotFound() {
        givenProductById(99L, null);

        Optional<Product> result = findById(99L);

        thenEmptyOptional(result);
    }

    @Test
    @DisplayName("Devuelve los productos mapeados de un tipo")
    void findByTypeShouldReturnMappedProductsOfType() {
        givenProductsByType(ProductType.PLAN, planEntity());

        List<Product> result = findByType(ProductType.PLAN);

        thenProductsOfTypeMapped(result);
    }

    @Test
    @DisplayName("Devuelve lista vacía cuando no hay productos del tipo")
    void findByTypeShouldReturnEmptyListWhenNoneOfType() {
        givenProductsByType(ProductType.COIN_PACK);

        List<Product> result = findByType(ProductType.COIN_PACK);

        thenEmptyList(result);
    }

    @Test
    @DisplayName("Devuelve los productos mapeados al buscar por ids")
    void findByIdsShouldReturnMappedProductsWhenFound() {
        givenProductsByIds(List.of(1L, 10L), coinPackEntity(), planEntity());

        List<Product> result = findByIds(List.of(1L, 10L));

        thenProductsByIdsMapped(result);
    }

    @Test
    @DisplayName("Guarda el producto mapeando dominio a entidad y de vuelta")
    void saveShouldMapDomainToEntityAndBack() {
        givenSavedEntity(coinPackEntity());

        Product result = save(coinPackDomain());

        thenSavedProductMapped(result);
    }

    @Test
    @DisplayName("Devuelve los productos activos de un tipo")
    void findByTypeAndActiveShouldReturnMappedProducts() {
        givenProductsByTypeAndActive(ProductType.COIN_PACK, true, coinPackEntity());

        List<Product> result = findByTypeAndActive(ProductType.COIN_PACK, true);

        thenProductsByTypeAndActiveMapped(result);
    }

    // --- arrange ---
    private void givenAllProducts(ProductEntity... entities) {
        when(jpaRepository.findAll()).thenReturn(List.of(entities));
    }

    private void givenProductById(Long id, ProductEntity entity) {
        when(jpaRepository.findById(id)).thenReturn(Optional.ofNullable(entity));
    }

    private void givenProductsByType(ProductType type, ProductEntity... entities) {
        when(jpaRepository.findByType(type)).thenReturn(List.of(entities));
    }

    private void givenProductsByIds(List<Long> ids, ProductEntity... entities) {
        when(jpaRepository.findAllById(ids)).thenReturn(List.of(entities));
    }

    private ProductEntity coinPackEntity() {
        return ProductEntity.builder()
                .id(1L).name("Pack Inicial").description("100 monedas")
                .price(new BigDecimal("499")).coinsAmount(100)
                .type(ProductType.COIN_PACK).planCode(null)
                .build();
    }

    private ProductEntity planEntity() {
        return ProductEntity.builder()
                .id(10L).name("Plan Premium").description("Acceso premium")
                .price(new BigDecimal("9999")).coinsAmount(0)
                .type(ProductType.PLAN).planCode("PREMIUM").chatDailyLimit(20)
                .build();
    }

    private void givenSavedEntity(ProductEntity entity) {
        when(jpaRepository.save(any(ProductEntity.class))).thenReturn(entity);
    }

    private void givenProductsByTypeAndActive(ProductType type, boolean active, ProductEntity... entities) {
        when(jpaRepository.findByTypeAndActive(type, active)).thenReturn(List.of(entities));
    }

    private Product coinPackDomain() {
        return Product.builder()
                .name("Pack Inicial").description("100 monedas")
                .price(new BigDecimal("499")).coinsAmount(100)
                .type(ProductType.COIN_PACK).active(true)
                .build();
    }

    // --- act ---
    private List<Product> findAll() {
        return repository.findAll();
    }

    private Optional<Product> findById(Long id) {
        return repository.findById(id);
    }

    private List<Product> findByType(ProductType type) {
        return repository.findByType(type);
    }

    private List<Product> findByIds(List<Long> ids) {
        return repository.findByIds(ids);
    }

    private Product save(Product product) {
        return repository.save(product);
    }

    private List<Product> findByTypeAndActive(ProductType type, boolean active) {
        return repository.findByTypeAndActive(type, active);
    }

    // --- assert ---
    private void thenAllProductsMapped(List<Product> result) {
        assertThat(result).hasSize(2);
        Product first = result.get(0);
        assertThat(first.getId()).isEqualTo(1L);
        assertThat(first.getName()).isEqualTo("Pack Inicial");
        assertThat(first.getDescription()).isEqualTo("100 monedas");
        assertThat(first.getPrice()).isEqualByComparingTo("499");
        assertThat(first.getCoinsAmount()).isEqualTo(100);
        assertThat(first.getType()).isEqualTo(ProductType.COIN_PACK);
        assertThat(first.getPlanCode()).isNull();
    }

    private void thenProductFoundById(Optional<Product> result) {
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(10L);
        assertThat(result.get().getType()).isEqualTo(ProductType.PLAN);
        assertThat(result.get().getPlanCode()).isEqualTo("PREMIUM");
        assertThat(result.get().getChatDailyLimit()).isEqualTo(20);
    }

    private void thenProductsOfTypeMapped(List<Product> result) {
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(ProductType.PLAN);
        assertThat(result.get(0).getName()).isEqualTo("Plan Premium");
    }

    private void thenProductsByIdsMapped(List<Product> result) {
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(10L);
    }

    private void thenEmptyList(List<Product> result) {
        assertThat(result).isEmpty();
    }

    private void thenEmptyOptional(Optional<Product> result) {
        assertThat(result).isEmpty();
    }

    private void thenSavedProductMapped(Product result) {
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Pack Inicial");
    }

    private void thenProductsByTypeAndActiveMapped(List<Product> result) {
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(ProductType.COIN_PACK);
        assertThat(result.get(0).isActive()).isTrue();
    }
}
