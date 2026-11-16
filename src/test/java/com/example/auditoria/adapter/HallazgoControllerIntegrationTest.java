package com.example.auditoria.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HallazgoControllerIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper json;

    String registrar(String titulo) throws Exception {
        String body = """
                {"titulo":"%s","descripcion":"d","areaResponsable":"Infraestructura","severidad":"ALTA","fechaDeteccion":"2026-08-01"}
                """.formatted(titulo);
        String res = mvc.perform(post("/api/hallazgos").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return json.readTree(res).get("hallazgoId").asText();
    }

    @Test
    void cicloCompletoPorHttp() throws Exception {
        String id = registrar("Contrasenas por defecto");
        mvc.perform(patch("/api/hallazgos/" + id + "/iniciar-remediacion").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"responsable\":\"Infra\",\"fechaLimite\":\"2026-08-20\",\"notas\":\"Rotar\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("EN_REMEDIACION"));
        mvc.perform(patch("/api/hallazgos/" + id + "/cerrar")).andExpect(status().isOk());
        mvc.perform(patch("/api/hallazgos/" + id + "/reabrir").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Reaparecio en QA\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/hallazgos/" + id))
                .andExpect(jsonPath("$.estado").value("REABIERTO"))
                .andExpect(jsonPath("$.planRemediacion.responsable").value("Infra"))
                .andExpect(jsonPath("$.fechaCierre").doesNotExist());
    }

    @Test
    void cerrarSinRemediacionRetorna400() throws Exception {
        String id = registrar("Puertos abiertos");
        mvc.perform(patch("/api/hallazgos/" + id + "/cerrar"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.tipo").value("IllegalStateException"));
        mvc.perform(patch("/api/hallazgos/" + id + "/reabrir").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.tipo").value("TransicionInvalidaException"));
        mvc.perform(get("/api/hallazgos/00000000-0000-0000-0000-000000000000")).andExpect(status().isNotFound());
    }
}
