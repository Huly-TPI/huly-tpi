package com.huly.backend.domain.repository;



import com.huly.backend.domain.model.Example;

/**
 * Puerto (interfaz) del repositorio.
 * El dominio define el contrato; la infra lo implementa.
 */
public interface ExampleRepository {

    Example save(Example example);

}