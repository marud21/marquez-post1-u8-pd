package com.example.auditoria.usecase;

import com.example.auditoria.domain.valueobject.EstadoHallazgo;
import com.example.auditoria.domain.valueobject.HallazgoId;
import com.example.auditoria.domain.valueobject.Severidad;
import com.example.auditoria.usecase.impl.CerrarHallazgoService;
import com.example.auditoria.usecase.impl.ConsultarHallazgoService;
import com.example.auditoria.usecase.impl.IniciarRemediacionService;
import com.example.auditoria.usecase.impl.ObtenerDashboardAuditoriaService;
import com.example.auditoria.usecase.impl.RegistrarHallazgoService;
import com.example.auditoria.usecase.port.CambioEstadoView;
import com.example.auditoria.usecase.port.DashboardAuditoriaView;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Casos de uso probados con dobles en memoria de ambos puertos: sin Spring, sin JPA. */
class CasosDeUsoTest {

    private final RepositorioEnMemoria repo = new RepositorioEnMemoria();
    private final HistorialEnMemoria historial = new HistorialEnMemoria();

    @Test
    void cadaTransicionAgregaExactamenteUnRegistroAlHistorial() {
        HallazgoId id = new RegistrarHallazgoService(repo, historial)
                .ejecutar("Backups sin cifrar", null, "TI", Severidad.CRITICA, LocalDate.now().minusDays(10), "auditor1");
        new IniciarRemediacionService(repo, historial).ejecutar(id, "Seguridad", LocalDate.now().plusDays(5), "AES", "jefe.ti");
        new CerrarHallazgoService(repo, historial).ejecutar(id, "jefe.ti");

        assertThat(new ConsultarHallazgoService(repo).buscarPorId(id).getEstado()).isEqualTo(EstadoHallazgo.CERRADO);
        List<CambioEstadoView> h = historial.listarPorHallazgo(id);
        assertThat(h).extracting(CambioEstadoView::estadoNuevo).containsExactly("ABIERTO", "EN_REMEDIACION", "CERRADO");
        assertThat(h).extracting(CambioEstadoView::estadoAnterior).containsExactly(null, "ABIERTO", "EN_REMEDIACION");
        assertThat(h.get(1).usuario()).isEqualTo("jefe.ti");
    }

    @Test
    void transicionFallidaNoDejaRastroEnElHistorial() {
        HallazgoId id = new RegistrarHallazgoService(repo, historial)
                .ejecutar("Puertos", null, "TI", Severidad.BAJA, LocalDate.now(), "auditor1");
        assertThatThrownBy(() -> new CerrarHallazgoService(repo, historial).ejecutar(id, "x"))
                .isInstanceOf(IllegalStateException.class);
        assertThat(historial.listarPorHallazgo(id)).hasSize(1);
    }

    @Test
    void dashboardAgregaPorSeveridadEstadoYArea() {
        RegistrarHallazgoService registrar = new RegistrarHallazgoService(repo, historial);
        HallazgoId a = registrar.ejecutar("A", null, "TI", Severidad.ALTA, LocalDate.now().minusDays(4), "u");
        registrar.ejecutar("B", null, "TI", Severidad.ALTA, LocalDate.now(), "u");
        new IniciarRemediacionService(repo, historial).ejecutar(a, "r", LocalDate.now(), null, "u");
        new CerrarHallazgoService(repo, historial).ejecutar(a, "u");

        DashboardAuditoriaView d = new ObtenerDashboardAuditoriaService(repo).ejecutar();
        assertThat(d.porSeveridad()).singleElement().satisfies(c -> assertThat(c.total()).isEqualTo(2));
        assertThat(d.porEstado()).hasSize(2);
        assertThat(d.promedioDiasCierrePorArea()).singleElement()
                .satisfies(p -> assertThat(p.promedioDias()).isEqualTo(4.0));
    }

    @Test
    void hallazgoInexistenteLanzaNotFound() {
        assertThatThrownBy(() -> new CerrarHallazgoService(repo, historial).ejecutar(HallazgoId.nuevo(), "u"))
                .isInstanceOf(HallazgoNotFoundException.class);
    }
}
