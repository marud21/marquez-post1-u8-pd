package com.example.auditoria.adapter;

import com.example.auditoria.adapter.out.persistence.HistorialCambioEstadoJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class DashboardHistorialIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper json;
    @Autowired
    HistorialCambioEstadoJpaRepository historialJpa;

    String registrar(String area, String severidad, LocalDate deteccion) throws Exception {
        String body = """
                {"titulo":"T","areaResponsable":"%s","severidad":"%s","fechaDeteccion":"%s"}
                """.formatted(area, severidad, deteccion);
        String res = mvc.perform(post("/api/hallazgos").header("X-Usuario", "auditor.interno")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn().getResponse().getContentAsString();
        return json.readTree(res).get("hallazgoId").asText();
    }

    void remediarYCerrar(String id) throws Exception {
        mvc.perform(patch("/api/hallazgos/" + id + "/iniciar-remediacion").header("X-Usuario", "jefe.infra")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"responsable\":\"Infra\",\"fechaLimite\":\"2026-12-31\"}")).andExpect(status().isOk());
        mvc.perform(patch("/api/hallazgos/" + id + "/cerrar").header("X-Usuario", "jefe.infra")).andExpect(status().isOk());
    }

    @Test
    void dashboardEHistorialConsistentesConLosHallazgos() throws Exception {
        String a = registrar("Infraestructura", "ALTA", LocalDate.now().minusDays(10));
        String b = registrar("Infraestructura", "CRITICA", LocalDate.now().minusDays(20));
        registrar("Finanzas", "ALTA", LocalDate.now());
        remediarYCerrar(a);
        remediarYCerrar(b);
        mvc.perform(patch("/api/hallazgos/" + b + "/reabrir").header("X-Usuario", "cumplimiento")
                .contentType(MediaType.APPLICATION_JSON).content("{\"motivo\":\"Reaparece en revision externa\"}"));
        remediarYCerrar(b); // REABIERTO -> EN_REMEDIACION -> CERRADO

        mvc.perform(get("/api/hallazgos/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.porSeveridad[?(@.categoria=='ALTA')].total").value(2))
                .andExpect(jsonPath("$.porEstado[?(@.categoria=='CERRADO')].total").value(2))
                .andExpect(jsonPath("$.porEstado[?(@.categoria=='ABIERTO')].total").value(1))
                .andExpect(jsonPath("$.promedioDiasCierrePorArea[0].categoria").value("Infraestructura"))
                .andExpect(jsonPath("$.promedioDiasCierrePorArea[0].promedioDias").value(15.0));

        mvc.perform(get("/api/hallazgos/" + b + "/historial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6))
                .andExpect(jsonPath("$[0].estadoNuevo").value("ABIERTO"))
                .andExpect(jsonPath("$[3].estadoNuevo").value("REABIERTO"))
                .andExpect(jsonPath("$[3].usuario").value("cumplimiento"))
                .andExpect(jsonPath("$[5].estadoAnterior").value("EN_REMEDIACION"));

        // transicion invalida: no inserta nada en la bitacora
        long antes = historialJpa.countByHallazgoId(a);
        mvc.perform(patch("/api/hallazgos/" + a + "/cerrar")).andExpect(status().isBadRequest());
        assertThat(historialJpa.countByHallazgoId(a)).isEqualTo(antes);
    }
}
