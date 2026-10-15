package com.canchas.reserva.service;

import com.canchas.reserva.dto.AuthDtos.*;
import com.canchas.reserva.entity.Rol;
import com.canchas.reserva.entity.Usuario;
import com.canchas.reserva.exception.BusinessException;
import com.canchas.reserva.repository.UsuarioRepository;
import com.canchas.reserva.security.CustomUserDetails;
import com.canchas.reserva.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 86400000L);

        authService = new AuthService(usuarioRepository, passwordEncoder, jwtService, authenticationManager);
    }

    @Test
    @DisplayName("HU-13: Debe registrar un nuevo deportista exitosamente con contraseña cifrada")
    void registrar_Exito() {
        RegisterRequest request = new RegisterRequest(
            "Samuel", "Correa", "samuel@javeriana.edu.co", "pass123", "3009998888", Rol.ROLE_USER
        );

        when(usuarioRepository.existsByEmail("samuel@javeriana.edu.co")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("$2a$12$hashedpassword");

        Usuario usuarioGuardado = new Usuario(
            "Samuel", "Correa", "samuel@javeriana.edu.co", "$2a$12$hashedpassword", "3009998888", Rol.ROLE_USER
        );
        usuarioGuardado.setId(25L);

        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);

        AuthResponse response = authService.registrar(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNotEmpty();
        assertThat(response.getEmail()).isEqualTo("samuel@javeriana.edu.co");
        assertThat(response.getRol()).isEqualTo(Rol.ROLE_USER);
        verify(passwordEncoder).encode("pass123");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("HU-13: Debe rechazar el registro si el email ya existe")
    void registrar_EmailDuplicado_LanzaExcepcion() {
        RegisterRequest request = new RegisterRequest(
            "Samuel", "Correa", "duplicado@javeriana.edu.co", "pass123", "3009998888", Rol.ROLE_USER
        );

        when(usuarioRepository.existsByEmail("duplicado@javeriana.edu.co")).thenReturn(true);

        assertThatThrownBy(() -> authService.registrar(request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Ya existe una cuenta registrada");

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debe autenticar credenciales válidas y retornar JWT")
    void login_Exito() {
        LoginRequest request = new LoginRequest("admin@javeriana.edu.co", "admin123");

        Usuario admin = new Usuario("Admin", "Javeriana", "admin@javeriana.edu.co", "hash", "123", Rol.ROLE_ADMIN);
        admin.setId(1L);
        CustomUserDetails userDetails = new CustomUserDetails(admin);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNotEmpty();
        assertThat(response.getRol()).isEqualTo(Rol.ROLE_ADMIN);
    }
}
