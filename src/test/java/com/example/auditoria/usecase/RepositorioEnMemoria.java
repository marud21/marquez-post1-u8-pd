package com.example.auditoria.usecase;

import com.example.auditoria.domain.entity.HallazgoAuditoria;
import com.example.auditoria.domain.valueobject.HallazgoId;
import com.example.auditoria.usecase.port.HallazgoRepositoryPort;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Doble de prueba del puerto: permite probar los casos de uso sin JPA ni Spring. */
public class RepositorioEnMemoria implements HallazgoRepositoryPort {

    protected final Map<HallazgoId, HallazgoAuditoria> datos = new LinkedHashMap<>();

    @Override
    public void guardar(HallazgoAuditoria hallazgo) {
        datos.put(hallazgo.getId(), hallazgo);
    }

    @Override
    public Optional<HallazgoAuditoria> buscarPorId(HallazgoId id) {
        return Optional.ofNullable(datos.get(id));
    }

    @Override
    public List<HallazgoAuditoria> buscarTodos() {
        return new ArrayList<>(datos.values());
    }
}
