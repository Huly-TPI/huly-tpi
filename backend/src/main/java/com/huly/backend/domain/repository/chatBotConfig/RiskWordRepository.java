package com.huly.backend.domain.repository.chatBotConfig;

import com.huly.backend.domain.model.RiskWord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de dominio para la persistencia de palabras de riesgo.
 * Define el contrato que debe cumplir cualquier implementación de
 * acceso a datos para esta entidad, siguiendo el principio de
 * inversión de dependencias de la arquitectura hexagonal.
 */
public interface RiskWordRepository {

    /**
     * Persiste una palabra de riesgo nueva o actualiza una existente.
     *
     * @param riskWord palabra de riesgo a guardar
     * @return la palabra de riesgo guardada con su id asignado
     */
    RiskWord save(RiskWord riskWord);

    /**
     * Busca una palabra de riesgo por su identificador.
     *
     * @param id identificador de la palabra de riesgo
     * @return un {@link Optional} con la palabra encontrada, o vacío si no existe
     */
    Optional<RiskWord> findById(Long id);

    /**
     * Elimina físicamente una palabra de riesgo por su identificador.
     *
     * @param id identificador de la palabra de riesgo a eliminar
     */
    void deleteById(Long id);

    /**
     * Verifica si existe una palabra de riesgo con el identificador dado.
     *
     * @param id identificador a verificar
     * @return {@code true} si existe, {@code false} en caso contrario
     */
    boolean existsById(Long id);

    /**
     * Verifica si ya existe una palabra de riesgo con el valor indicado, ignorando mayúsculas.
     *
     * @param word valor de la palabra a verificar
     * @return {@code true} si ya existe, {@code false} en caso contrario
     */
    boolean existsByWordIgnoreCase(String word);

    /**
     * Verifica si existe otra palabra de riesgo con el mismo valor (ignorando mayúsculas),
     * excluyendo el registro con el id proporcionado.
     *
     * @param word valor de la palabra a verificar
     * @param id   identificador del registro a excluir de la búsqueda
     * @return {@code true} si existe otro registro con ese valor, {@code false} en caso contrario
     */
    boolean existsByWordIgnoreCaseAndIdNot(String word, Long id);

    /**
     * Recupera una página de palabras de riesgo aplicando filtros opcionales.
     *
     * @param word     fragmento de texto para filtrar por contenido de la palabra (puede ser {@code null})
     * @param active   estado activo/inactivo para filtrar (puede ser {@code null})
     * @param severity nivel de severidad para filtrar, como cadena de texto (puede ser {@code null})
     * @param pageable configuración de paginación y ordenamiento
     * @return página de palabras de riesgo que coinciden con los filtros
     */
    Page<RiskWord> findAll(String word, Boolean active, String severity, Pageable pageable);

    /**
     * Recupera todas las palabras de riesgo que se encuentran activas.
     *
     * @return lista de palabras de riesgo con estado activo
     */
    List<RiskWord> findAllActive();
}
