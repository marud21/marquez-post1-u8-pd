package com.example.auditoria.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HallazgoJpaRepository extends JpaRepository<HallazgoJpaEntity, String> {

    // Interface projections sobre el mismo esquema: GROUP BY resuelto por el motor de base de datos
    @Query("SELECT str(h.severidad) AS categoria, COUNT(h) AS total FROM HallazgoJpaEntity h "
            + "GROUP BY h.severidad ORDER BY h.severidad")
    List<ConteoProjection> contarPorSeveridad();

    @Query("SELECT str(h.estado) AS categoria, COUNT(h) AS total FROM HallazgoJpaEntity h "
            + "GROUP BY h.estado ORDER BY h.estado")
    List<ConteoProjection> contarPorEstado();

    @Query("SELECT h.areaResponsable AS categoria, "
            + "AVG(1.0 * ((h.fechaCierre - h.fechaDeteccion) BY DAY)) AS promedio "
            + "FROM HallazgoJpaEntity h WHERE h.estado = com.example.auditoria.domain.valueobject.EstadoHallazgo.CERRADO "
            + "GROUP BY h.areaResponsable ORDER BY h.areaResponsable")
    List<PromedioProjection> promedioDiasCierrePorArea();

    interface ConteoProjection {
        String getCategoria();

        Long getTotal();
    }

    interface PromedioProjection {
        String getCategoria();

        Double getPromedio();
    }
}
