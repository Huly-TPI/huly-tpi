package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.infrastructure.repository.entity.RiskWordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Repositorio JPA para la entidad {@link RiskWordEntity}.
 * Extiende {@link JpaRepository} para las operaciones CRUD estándar y
 * {@link JpaSpecificationExecutor} para habilitar consultas dinámicas
 * mediante la API de Specification.
 */
public interface IRiskWordJpaRepository
        extends JpaRepository<RiskWordEntity, Long>, JpaSpecificationExecutor<RiskWordEntity> {

    /**
     * Verifica si existe una palabra de riesgo con el valor exacto indicado.
     *
     * @param word valor a buscar
     * @return {@code true} si existe al menos un registro con ese valor
     */
    boolean existsByWord(String word);

    /**
     * Verifica si existe otra palabra de riesgo con el mismo valor,
     * excluyendo el registro con el id proporcionado.
     *
     * @param word valor a buscar
     * @param id   id del registro a excluir de la búsqueda
     * @return {@code true} si otro registro distinto tiene ese valor
     */
    boolean existsByWordAndIdNot(String word, Long id);
}
