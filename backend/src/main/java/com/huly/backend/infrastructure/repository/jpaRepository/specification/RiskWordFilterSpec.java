package com.huly.backend.infrastructure.repository.jpaRepository.specification;

import com.huly.backend.domain.model.enums.RiskSeverity;
import com.huly.backend.infrastructure.repository.entity.RiskWordEntity;
import org.springframework.data.jpa.domain.Specification;

/**
 * Clase utilitaria que construye una {@link Specification} de JPA para consultas
 * dinámicas sobre {@link RiskWordEntity}. Cada predicado privado retorna {@code null}
 * cuando su parámetro es ausente; el método {@code build} los encadena solo si
 * no son nulos, para compatibilidad con Spring Data JPA 4.x.
 */
public class RiskWordFilterSpec {

    private RiskWordFilterSpec() {}

    /**
     * Construye la especificación combinada a partir de los filtros opcionales.
     * Los filtros nulos o en blanco se omiten sin generar ningún predicado SQL.
     *
     * @param word     fragmento de texto para filtrar por contenido de la palabra (puede ser {@code null})
     * @param active   estado activo/inactivo para filtrar (puede ser {@code null})
     * @param severity nivel de severidad como texto para filtrar (puede ser {@code null})
     * @return {@link Specification} lista para pasar a {@link org.springframework.data.jpa.repository.JpaSpecificationExecutor#findAll}
     */
    public static Specification<RiskWordEntity> build(String word, Boolean active, String severity) {
        Specification<RiskWordEntity> spec = (root, query, cb) -> cb.conjunction();

        Specification<RiskWordEntity> wordSpec = wordContains(word);
        if (wordSpec != null) spec = spec.and(wordSpec);

        Specification<RiskWordEntity> activeSpec = isActive(active);
        if (activeSpec != null) spec = spec.and(activeSpec);

        Specification<RiskWordEntity> severitySpec = hasSeverity(severity);
        if (severitySpec != null) spec = spec.and(severitySpec);

        return spec;
    }

    /**
     * Predicado que filtra palabras cuyo valor contiene el fragmento indicado,
     * ignorando mayúsculas y minúsculas.
     *
     * @param word fragmento a buscar; retorna {@code null} si es nulo o en blanco
     * @return especificación de búsqueda parcial, o {@code null} si no aplica
     */
    private static Specification<RiskWordEntity> wordContains(String word) {
        if (word == null || word.isBlank()) return null;
        return (root, query, builder) ->
                builder.like(builder.lower(root.get("word")), "%" + word.toLowerCase() + "%");
    }

    /**
     * Predicado que filtra por el estado activo o inactivo del registro.
     *
     * @param active valor booleano a comparar; retorna {@code null} si es nulo
     * @return especificación de igualdad sobre el campo {@code active}, o {@code null} si no aplica
     */
    private static Specification<RiskWordEntity> isActive(Boolean active) {
        if (active == null) return null;
        return (root, query, builder) ->
                builder.equal(root.get("active"), active);
    }

    /**
     * Predicado que filtra por nivel de severidad. Convierte el texto al enum
     * {@link RiskSeverity} antes de construir el predicado, normalizando el input
     * a mayúsculas para ignorar diferencias de capitalización.
     *
     * @param severity texto del nivel de severidad; retorna {@code null} si es nulo o en blanco
     * @return especificación de igualdad sobre el campo {@code severity}, o {@code null} si no aplica
     */
    private static Specification<RiskWordEntity> hasSeverity(String severity) {
        if (severity == null || severity.isBlank()) return null;
        return (root, query, builder) ->
                builder.equal(root.<RiskSeverity>get("severity"), RiskSeverity.valueOf(severity.toUpperCase()));
    }
}
