package com.canchas.reserva.service;

import com.canchas.reserva.dto.AuthDtos.*;
import com.canchas.reserva.entity.Rol;
import com.canchas.reserva.entity.Usuario;
import com.canchas.reserva.exception.BusinessException;
import com.canchas.reserva.exception.ResourceNotFoundException;
import com.canchas.reserva.repository.UsuarioRepository;
import com.canchas.reserva.security.CustomUserDetails;
import com.canchas.reserva.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse registrar(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Ya existe una cuenta registrada con el correo: " + request.getEmail());
        }

        Usuario usuario = new Usuario(
            request.getNombre(),
            request.getApellido(),
            request.getEmail(),
            passwordEncoder.encode(request.getPassword()),
            request.getTelefono(),
            Rol.ROLE_USER // Seguridad estricta: Todo autoregistro asigna inmutablemente Rol.ROLE_USER
        );

        Usuario guardado = usuarioRepository.save(usuario);
        CustomUserDetails userDetails = new CustomUserDetails(guardado);
        String token = jwtService.generateToken(userDetails, guardado.getRol().name());

        return new AuthResponse(
            token,
            guardado.getId(),
            guardado.getNombre(),
            guardado.getApellido(),
            guardado.getEmail(),
            guardado.getRol()
        );
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Usuario usuario = userDetails.getUsuario();
        String token = jwtService.generateToken(userDetails, usuario.getRol().name());

        return new AuthResponse(
            token,
            usuario.getId(),
            usuario.getNombre(),
            usuario.getApellido(),
            usuario.getEmail(),
            usuario.getRol()
        );
    }

    public UsuarioDto obtenerPerfil(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));

        return new UsuarioDto(
            usuario.getId(),
            usuario.getNombre(),
            usuario.getApellido(),
            usuario.getEmail(),
            usuario.getTelefono(),
            usuario.getRol(),
            usuario.isActivo()
        );
    }
}
