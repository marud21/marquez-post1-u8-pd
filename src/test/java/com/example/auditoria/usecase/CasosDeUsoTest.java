package com.example.auditoria.usecase;

import com.example.auditoria.domain.valueobject.EstadoHallazgo;
import com.example.auditoria.domain.valueobject.HallazgoId;
import com.example.auditoria.domain.valueobject.Severidad;
import com.example.auditoria.usecase.impl.CerrarHallazgoService;
import com.example.auditoria.usecase.impl.ConsultarHallazgoService;
import com.example.auditoria.usecase.impl.IniciarRemediacionService;
import com.example.auditoria.usecase.impl.RegistrarHallazgoService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CasosDeUsoTest {

    private final RepositorioEnMemoria repo = new RepositorioEnMemoria();

    @Test
    void registrarIniciarYCerrar() {
        HallazgoId id = new RegistrarHallazgoService(repo)
                .ejecutar("Backups sin cifrar", null, "TI", Severidad.CRITICA, LocalDate.of(2026, 7, 1));
        new IniciarRemediacionService(repo).ejecutar(id, "Seguridad", LocalDate.of(2026, 9, 1), "Cifrar AES-256");
        new CerrarHallazgoService(repo).ejecutar(id);

        assertThat(new ConsultarHallazgoService(repo).buscarPorId(id).getEstado()).isEqualTo(EstadoHallazgo.CERRADO);
    }

    @Test
    void hallazgoInexistenteLanzaNotFound() {
        assertThatThrownBy(() -> new CerrarHallazgoService(repo).ejecutar(HallazgoId.nuevo()))
                .isInstanceOf(HallazgoNotFoundException.class);
    }
}
