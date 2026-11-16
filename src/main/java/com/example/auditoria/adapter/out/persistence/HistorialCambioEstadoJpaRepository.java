package com.example.auditoria.adapter.out.persistence;

import org.springframework.data.repository.Repository;

import java.util.List;

/**
 * Se extiende Repository (no JpaRepository) a proposito: solo expone save y consultas,
 * de modo que no existe ningun metodo delete* que permita borrar la bitacora.
 */
public interface HistorialCambioEstadoJpaRepository extends Repository<HistorialCambioEstadoJpaEntity, Long> {

    HistorialCambioEstadoJpaEntity save(HistorialCambioEstadoJpaEntity entidad);

    List<HistorialCambioEstadoJpaEntity> findByHallazgoIdOrderByFechaAscIdAsc(String hallazgoId);

    long countByHallazgoId(String hallazgoId);
}
