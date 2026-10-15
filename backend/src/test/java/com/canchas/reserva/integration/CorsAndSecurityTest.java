package com.canchas.reserva.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CorsAndSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Seguridad: Acceso sin token a endpoint protegido debe ser denegado (403 Forbidden)")
    void accesoSinToken_Retorna403() throws Exception {
        mockMvc.perform(get("/api/reservas/mis-reservas"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Seguridad: Endpoints públicos como /api/escenarios deben permitir acceso libre")
    void accesoPublicoEscenarios_Retorna200() throws Exception {
        mockMvc.perform(get("/api/escenarios"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Seguridad: Las respuestas deben contener X-Frame-Options SAMEORIGIN contra Clickjacking")
    void cabecerasSeguridad_ContieneFrameOptionsSameOrigin() throws Exception {
        mockMvc.perform(get("/api/escenarios"))
            .andExpect(header().string("X-Frame-Options", "SAMEORIGIN"));
    }

    @Test
    @DisplayName("CORS: Petición Preflight OPTIONS desde origen permitido debe responder 200 con cabeceras CORS")
    void corsPreflight_OrigenPermitido_RetornaCabecerasValidas() throws Exception {
        mockMvc.perform(options("/api/escenarios")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000"))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }
}
