package com.example.auditoria.adapter.in.web.dto;

import com.example.auditoria.domain.entity.HallazgoAuditoria;
import com.example.auditoria.domain.valueobject.PlanRemediacion;

import java.time.LocalDate;

/** DTO de salida del circulo Interface Adapters: el dominio nunca se serializa directamente. */
public record HallazgoResponse(
        String id,
        String titulo,
        String descripcion,
        String areaResponsable,
        String severidad,
        String estado,
        LocalDate fechaDeteccion,
        LocalDate fechaCierre,
        PlanRemediacion planRemediacion
) {
    public static HallazgoResponse desde(HallazgoAuditoria h) {
        return new HallazgoResponse(h.getId().toString(), h.getTitulo(), h.getDescripcion(),
                h.getAreaResponsable(), h.getSeveridad().name(), h.getEstado().name(),
                h.getFechaDeteccion(), h.getFechaCierre(), h.getPlanRemediacion());
    }
}
