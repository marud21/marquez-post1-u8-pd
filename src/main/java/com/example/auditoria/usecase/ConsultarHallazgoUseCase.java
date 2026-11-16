package com.example.auditoria.usecase;

import com.example.auditoria.domain.entity.HallazgoAuditoria;
import com.example.auditoria.domain.valueobject.HallazgoId;

import java.util.List;

/**
 * Devuelve objetos de dominio, no DTOs web: el mapeo a HallazgoResponse ocurre en el
 * Interface Adapter, para que usecase/ no dependa de un circulo exterior.
 */
public interface ConsultarHallazgoUseCase {

    HallazgoAuditoria buscarPorId(HallazgoId id);

    List<HallazgoAuditoria> listarTodos();
}
