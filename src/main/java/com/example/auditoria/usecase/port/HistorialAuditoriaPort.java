package com.example.auditoria.usecase.port;

import com.example.auditoria.domain.valueobject.EstadoHallazgo;
import com.example.auditoria.domain.valueobject.HallazgoId;

import java.util.List;

/**
 * Bitacora append-only de cambios de estado: un solo puerto para escribir y leer.
 * No es la fuente de verdad del estado (eso sigue siendo HallazgoRepositoryPort).
 */
public interface HistorialAuditoriaPort {

    /** @param anterior null cuando se trata del registro inicial del hallazgo */
    void registrar(HallazgoId hallazgoId, EstadoHallazgo anterior, EstadoHallazgo nuevo, String motivo, String usuario);

    List<CambioEstadoView> listarPorHallazgo(HallazgoId hallazgoId);
}
