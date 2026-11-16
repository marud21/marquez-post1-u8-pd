package com.example.auditoria.usecase;

import com.example.auditoria.domain.valueobject.EstadoHallazgo;
import com.example.auditoria.domain.valueobject.HallazgoId;
import com.example.auditoria.usecase.port.CambioEstadoView;
import com.example.auditoria.usecase.port.HistorialAuditoriaPort;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HistorialEnMemoria implements HistorialAuditoriaPort {

    private final List<Object[]> registros = new ArrayList<>();

    @Override
    public void registrar(HallazgoId id, EstadoHallazgo anterior, EstadoHallazgo nuevo, String motivo, String usuario) {
        registros.add(new Object[]{id, new CambioEstadoView(anterior == null ? null : anterior.name(), nuevo.name(),
                motivo, usuario, LocalDateTime.now())});
    }

    @Override
    public List<CambioEstadoView> listarPorHallazgo(HallazgoId id) {
        return registros.stream().filter(r -> r[0].equals(id)).map(r -> (CambioEstadoView) r[1]).toList();
    }
}
