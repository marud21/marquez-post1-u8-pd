package com.example.auditoria.domain;

import com.example.auditoria.domain.entity.HallazgoAuditoria;
import com.example.auditoria.domain.valueobject.EstadoHallazgo;
import com.example.auditoria.domain.valueobject.HallazgoId;
import com.example.auditoria.domain.valueobject.PlanRemediacion;
import com.example.auditoria.domain.valueobject.Severidad;
import com.example.auditoria.domain.valueobject.TransicionInvalidaException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Prueba del agregado sin @SpringBootTest: el dominio es Java puro. */
class HallazgoAuditoriaTest {

    private HallazgoAuditoria nuevo() {
        return new HallazgoAuditoria(HallazgoId.nuevo(), "Credenciales por defecto", "QA",
                "Infraestructura", Severidad.ALTA, LocalDate.of(2026, 8, 1));
    }

    private PlanRemediacion plan() {
        return new PlanRemediacion("Equipo Infra", LocalDate.of(2026, 8, 20), "Rotar credenciales");
    }

    @Test
    void cicloDeVidaCompletoRetornaElEstadoAnterior() {
        HallazgoAuditoria h = nuevo();
        assertThat(h.iniciarRemediacion(plan())).isEqualTo(EstadoHallazgo.ABIERTO);
        assertThat(h.cerrar()).isEqualTo(EstadoHallazgo.EN_REMEDIACION);
        assertThat(h.getFechaCierre()).isNotNull();
        assertThat(h.reabrir()).isEqualTo(EstadoHallazgo.CERRADO);
        assertThat(h.getFechaCierre()).isNull();
        assertThat(h.iniciarRemediacion(plan())).isEqualTo(EstadoHallazgo.REABIERTO);
        assertThat(h.getEstado()).isEqualTo(EstadoHallazgo.EN_REMEDIACION);
    }

    @Test
    void noSePuedeCerrarSinPlanNiReabrirUnoAbierto() {
        HallazgoAuditoria h = nuevo();
        assertThatThrownBy(h::cerrar).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(h::reabrir).isInstanceOf(TransicionInvalidaException.class)
                .hasMessage("No se puede transicionar de ABIERTO a REABIERTO");
    }

    @Test
    void laMaquinaDeEstadosSoloAceptaLasTransicionesDocumentadas() {
        for (EstadoHallazgo origen : EstadoHallazgo.values()) {
            long validos = java.util.Arrays.stream(EstadoHallazgo.values()).filter(origen::puedeTransicionarA).count();
            assertThat(validos).as("destinos validos desde " + origen).isEqualTo(1);
        }
    }

    @Test
    void planDeRemediacionRequiereResponsable() {
        assertThatThrownBy(() -> new PlanRemediacion(" ", LocalDate.now(), null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
