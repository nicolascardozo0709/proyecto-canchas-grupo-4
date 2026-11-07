package com.canchas.reserva.controller;

import com.canchas.reserva.dto.AiDtos.*;
import com.canchas.reserva.dto.ReservaDtos.ReservaRequest;
import com.canchas.reserva.dto.ReservaDtos.ReservaResponse;
import com.canchas.reserva.entity.Usuario;
import com.canchas.reserva.repository.UsuarioRepository;
import com.canchas.reserva.security.CustomUserDetails;
import com.canchas.reserva.service.GeminiService;
import com.canchas.reserva.service.ReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para el Asistente Deportivo Inteligente potenciado por Google AI Studio / Gemini.
 */
@RestController
@RequestMapping("/api/ai")
@Tag(name = "Asistente Deportivo IA", description = "Endpoints para interacción conversacional y reservas autónomas con Gemini AI")
public class AiAssistantController {

    private final GeminiService geminiService;
    private final ReservaService reservaService;
    private final UsuarioRepository usuarioRepository;

    public AiAssistantController(GeminiService geminiService,
                                 ReservaService reservaService,
                                 UsuarioRepository usuarioRepository) {
        this.geminiService = geminiService;
        this.reservaService = reservaService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/chat")
    @Operation(summary = "Consultar al Asistente Deportivo Inteligente en lenguaje natural")
    public ResponseEntity<ChatResponse> consultarAsistente(
            @RequestBody ChatRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Usuario usuario = null;
        if (userDetails != null && userDetails.getId() != null) {
            usuario = usuarioRepository.findById(userDetails.getId()).orElse(null);
        }

        ChatResponse response = geminiService.procesarConsulta(request, usuario);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/auto-reservar")
    @Operation(summary = "Ejecutar reserva autónoma generada por el Asistente Deportivo")
    public ResponseEntity<ReservaResponse> ejecutarAutoReserva(
            @RequestBody ReservaRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ReservaResponse reservaConfirmada = reservaService.crearReserva(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaConfirmada);
    }
}
