package com.canchas.reserva.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integración del módulo de datos (Entrega 3) contra la base MySQL de pruebas:
 * control de acceso (solo ADMIN), filtros del reporte, estadísticas y exportación CSV.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ReporteIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private JsonNode login(String email, String password) throws Exception {
        MvcResult res = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString());
    }

    private String tokenAdmin() throws Exception { return login("admin@canchas.com", "admin123").get("token").asText(); }
    private String tokenUsuario() throws Exception { return login("usuario@canchas.com", "user123").get("token").asText(); }

    @Test
    @DisplayName("Seguridad: sin token o con rol USER los endpoints de reportes responden 403")
    void soloAdministrador() throws Exception {
        String[] rutas = {"/api/reportes/estadisticas", "/api/reportes/reservas", "/api/reportes/reservas/csv", "/api/reportes/usuarios"};
        String tokenUser = tokenUsuario();
        for (String ruta : rutas) {
            mockMvc.perform(get(ruta)).andExpect(status().isForbidden());
            mockMvc.perform(get(ruta).header("Authorization", "Bearer " + tokenUser)).andExpect(status().isForbidden());
        }
    }

    @Test
    @DisplayName("Reporte filtrable + CSV + estadísticas: una reserva creada aparece en los tres, y los filtros la excluyen cuando no coinciden")
    void flujoReporteCompleto() throws Exception {
        JsonNode user = login("usuario@canchas.com", "user123");
        long usuarioId = user.get("id").asLong();
        String tokenUser = user.get("token").asText();
        String tokenAdmin = tokenAdmin();

        // Fecha única para no interferir con otras pruebas: una reserva del deportista en escenario 1, franja 3
        LocalDate fecha = LocalDate.now().plusDays(41);
        MvcResult creada = mockMvc.perform(post("/api/reservas")
                .header("Authorization", "Bearer " + tokenUser)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"escenarioId\":1,\"franjaId\":3,\"fecha\":\"" + fecha + "\"}"))
            .andExpect(status().isCreated())
            .andReturn();
        String codigo = objectMapper.readTree(creada.getResponse().getContentAsString()).get("codigoReserva").asText();

        // 1) Reporte filtrado por escenario + usuario + rango de un solo día
        mockMvc.perform(get("/api/reportes/reservas")
                .header("Authorization", "Bearer " + tokenAdmin)
                .param("escenarioId", "1").param("usuarioId", String.valueOf(usuarioId))
                .param("desde", fecha.toString()).param("hasta", fecha.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].codigoReserva").value(codigo))
            .andExpect(jsonPath("$[0].estado").value("CONFIRMADA"));

        // 2) Otro escenario en la misma fecha no devuelve nada
        mockMvc.perform(get("/api/reportes/reservas")
                .header("Authorization", "Bearer " + tokenAdmin)
                .param("escenarioId", "2")
                .param("desde", fecha.toString()).param("hasta", fecha.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));

        // 3) Otra fecha (día siguiente) tampoco
        mockMvc.perform(get("/api/reportes/reservas")
                .header("Authorization", "Bearer " + tokenAdmin)
                .param("desde", fecha.plusDays(1).toString()).param("hasta", fecha.plusDays(1).toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));

        // 4) Filtro por estado: la reserva no está cancelada
        mockMvc.perform(get("/api/reportes/reservas")
                .header("Authorization", "Bearer " + tokenAdmin)
                .param("desde", fecha.toString()).param("hasta", fecha.toString()).param("estado", "CANCELADA"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));

        // 5) CSV: UTF-8 con BOM, separador ';' y la reserva en el contenido
        MvcResult csv = mockMvc.perform(get("/api/reportes/reservas/csv")
                .header("Authorization", "Bearer " + tokenAdmin)
                .param("desde", fecha.toString()).param("hasta", fecha.toString()).param("separador", ";"))
            .andExpect(status().isOk())
            .andReturn();
        assertThat(csv.getResponse().getContentType()).startsWith("text/csv");
        assertThat(csv.getResponse().getHeader("Content-Disposition")).contains("attachment").contains(".csv");
        String cuerpo = csv.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertThat(cuerpo).startsWith("\uFEFFCódigo;Deportista;");
        assertThat(cuerpo).contains(codigo).contains("CONFIRMADA");

        // 6) Estadísticas del mismo día: 1 reserva vigente en el escenario 1 y 7 días de la semana en la respuesta
        mockMvc.perform(get("/api/reportes/estadisticas")
                .header("Authorization", "Bearer " + tokenAdmin)
                .param("desde", fecha.toString()).param("hasta", fecha.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.diasRango").value(1))
            .andExpect(jsonPath("$.totalVigentes").value(1))
            .andExpect(jsonPath("$.porDiaSemana.length()").value(7))
            .andExpect(jsonPath("$.porEscenario[?(@.escenarioId==1)].reservas").value(1))
            .andExpect(jsonPath("$.horaPico").isNotEmpty());
    }

    @Test
    @DisplayName("Validaciones: rango invertido y separador inválido devuelven 400; el listado de usuarios no expone contraseñas")
    void validacionesYPrivacidad() throws Exception {
        String tokenAdmin = tokenAdmin();

        mockMvc.perform(get("/api/reportes/estadisticas")
                .header("Authorization", "Bearer " + tokenAdmin)
                .param("desde", "2026-12-31").param("hasta", "2026-01-01"))
            .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/reportes/reservas/csv")
                .header("Authorization", "Bearer " + tokenAdmin)
                .param("separador", "|"))
            .andExpect(status().isBadRequest());

        MvcResult usuarios = mockMvc.perform(get("/api/reportes/usuarios")
                .header("Authorization", "Bearer " + tokenAdmin))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").isNotEmpty())
            .andExpect(jsonPath("$[0].email").isNotEmpty())
            .andReturn();
        String cuerpo = usuarios.getResponse().getContentAsString();
        assertThat(cuerpo.toLowerCase()).doesNotContain("password").doesNotContain("$2a$");
    }
}
