package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.RiskWord;
import com.huly.backend.domain.repository.RiskWordRepository;
import com.huly.backend.infrastructure.repository.entity.RiskWordEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IRiskWordJpaRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.specification.RiskWordFilterSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador de infraestructura que implementa el puerto de dominio {@link RiskWordRepository}.
 * Traduce entre el modelo de dominio {@link RiskWord} y la entidad JPA {@link RiskWordEntity},
 * y delega la persistencia al repositorio Spring Data {@link IRiskWordJpaRepository}.
 */
@Component
@RequiredArgsConstructor
public class RiskWordRepositoryImpl implements RiskWordRepository {

    private final IRiskWordJpaRepository jpa;

    /**
     * Convierte el dominio a entidad, persiste y retorna el dominio resultante.
     *
     * @param riskWord modelo de dominio a guardar
     * @return dominio con el id asignado por la base de datos
     */
    @Override
    public RiskWord save(RiskWord riskWord) {
        RiskWordEntity saved = jpa.save(toEntity(riskWord));
        return toDomain(saved);
    }

    /**
     * Busca una entidad por id y la convierte al modelo de dominio si existe.
     *
     * @param id identificador a buscar
     * @return {@link Optional} con el dominio encontrado, o vacío si no existe
     */
    @Override
    public Optional<RiskWord> findById(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    /**
     * Elimina el registro con el id indicado de la base de datos.
     *
     * @param id identificador del registro a eliminar
     */
    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsById(Long id) {
        return jpa.existsById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsByWord(String word) {
        return jpa.existsByWord(word);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsByWordAndIdNot(String word, Long id) {
        return jpa.existsByWordAndIdNot(word, id);
    }

    /**
     * Construye la {@link org.springframework.data.jpa.domain.Specification} con
     * {@link RiskWordFilterSpec}, ejecuta la consulta paginada y mapea cada entidad al dominio.
     *
     * @param word     fragmento de texto para filtrar (puede ser {@code null})
     * @param active   estado activo/inactivo para filtrar (puede ser {@code null})
     * @param severity texto de severidad para filtrar (puede ser {@code null})
     * @param pageable paginación y ordenamiento
     * @return página de modelos de dominio resultantes
     */
    @Override
    public Page<RiskWord> findAll(String word, Boolean active, String severity, Pageable pageable) {
        return jpa.findAll(RiskWordFilterSpec.build(word, active, severity), pageable)
                .map(this::toDomain);
    }

    /**
     * Convierte un modelo de dominio {@link RiskWord} a su representación JPA {@link RiskWordEntity}.
     *
     * @param d modelo de dominio origen
     * @return entidad JPA lista para persistir
     */
    private RiskWordEntity toEntity(RiskWord d) {
        return RiskWordEntity.builder()
                .id(d.getId())
                .word(d.getWord())
                .description(d.getDescription())
                .severity(d.getSeverity())
                .active(d.isActive())
                .build();
    }

    /**
     * Convierte una entidad JPA {@link RiskWordEntity} al modelo de dominio {@link RiskWord}.
     *
     * @param e entidad JPA origen
     * @return modelo de dominio resultante
     */
    private RiskWord toDomain(RiskWordEntity e) {
        return RiskWord.builder()
                .id(e.getId())
                .word(e.getWord())
                .description(e.getDescription())
                .severity(e.getSeverity())
                .active(e.isActive())
                .build();
    }
}
