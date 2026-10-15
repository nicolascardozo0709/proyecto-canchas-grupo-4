package com.canchas.reserva.controller;

import com.canchas.reserva.dto.AuthDtos.*;
import com.canchas.reserva.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints para registro de deportistas, inicio de sesión y consulta de perfil")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrar un nuevo deportista (HU-13)")
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión y obtener token JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/perfil")
    @Operation(summary = "Obtener los datos del usuario autenticado (HU-14)")
    public ResponseEntity<UsuarioDto> obtenerPerfil(@AuthenticationPrincipal UserDetails userDetails) {
        UsuarioDto perfil = authService.obtenerPerfil(userDetails.getUsername());
        return ResponseEntity.ok(perfil);
    }
}
