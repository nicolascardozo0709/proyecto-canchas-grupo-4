package com.canchas.reserva.integration;

import com.canchas.reserva.dto.AuthDtos.LoginRequest;
import com.canchas.reserva.dto.AuthDtos.RegisterRequest;
import com.canchas.reserva.dto.ReservaDtos.ReservaRequest;
import com.canchas.reserva.entity.Rol;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReservaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Flujo de Integración Completo: Registro -> Login -> Reserva -> Colisión (409) -> Cancelación -> Re-reserva")
    void testFlujoCompletoDeReserva() throws Exception {
        // 1. Registro de Usuario Deportista
        RegisterRequest registerReq = new RegisterRequest(
            "Mateo", "Ríos", "mateo.rios@javeriana.edu.co", "password123", "3150001122", Rol.ROLE_USER
        );

        mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerReq)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.email").value("mateo.rios@javeriana.edu.co"));

        // 2. Login para obtener Token JWT
        LoginRequest loginReq = new LoginRequest("mateo.rios@javeriana.edu.co", "password123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = loginJson.get("token").asText();

        // 3. Crear una reserva exitosa para el escenario 1, franja 1 en fecha futura
        LocalDate fechaReserva = LocalDate.now().plusDays(5);
        ReservaRequest reservaReq = new ReservaRequest(1L, 1L, fechaReserva);

        MvcResult reservaResult = mockMvc.perform(post("/api/reservas")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reservaReq)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.codigoReserva").isNotEmpty())
            .andExpect(jsonPath("$.estado").value("CONFIRMADA"))
            .andReturn();

        JsonNode reservaJson = objectMapper.readTree(reservaResult.getResponse().getContentAsString());
        Long reservaId = reservaJson.get("id").asLong();

        // 4. HU-07: Intentar reservar la MISMA franja en la MISMA fecha -> Debe retornar 409 Conflict
        mockMvc.perform(post("/api/reservas")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reservaReq)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("CONFLICTO_COLISION_HORARIO"));

        // 5. HU-06: Cancelar la reserva
        mockMvc.perform(put("/api/reservas/" + reservaId + "/cancelar")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"motivo\": \"Cambio de planes de entrenamiento\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("CANCELADA"));

        // 6. HU-06: Tras la cancelación, la franja queda liberada de inmediato y puede reservarse de nuevo
        mockMvc.perform(post("/api/reservas")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reservaReq)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.estado").value("CONFIRMADA"));
    }

    @Test
    @DisplayName("HU-08: Debe rechazar reservas con fecha pasada retornando 400 Bad Request")
    void testRechazoFechaPasada() throws Exception {
        LoginRequest loginReq = new LoginRequest("admin@javeriana.edu.co", "admin123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
            .andExpect(status().isOk())
            .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

        LocalDate fechaAyer = LocalDate.now().minusDays(1);
        ReservaRequest reservaPasada = new ReservaRequest(1L, 2L, fechaAyer);

        mockMvc.perform(post("/api/reservas")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reservaPasada)))
            .andExpect(status().isBadRequest());
    }
}
