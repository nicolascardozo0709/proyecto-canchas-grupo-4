package com.canchas.reserva.dto;

import com.canchas.reserva.entity.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {

    public static class LoginRequest {
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe ser válido")
        private String email;

        @NotBlank(message = "La contraseña es obligatoria")
        private String password;

        public LoginRequest() {}
        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RegisterRequest {
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        private String nombre;

        @NotBlank(message = "El apellido es obligatorio")
        @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
        private String apellido;

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe ser un formato válido")
        private String email;

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener mínimo 6 caracteres")
        private String password;

        private String telefono;
        private Rol rol = Rol.ROLE_USER;

        public RegisterRequest() {}
        public RegisterRequest(String nombre, String apellido, String email, String password, String telefono, Rol rol) {
            this.nombre = nombre;
            this.apellido = apellido;
            this.email = email;
            this.password = password;
            this.telefono = telefono;
            this.rol = rol != null ? rol : Rol.ROLE_USER;
        }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getApellido() { return apellido; }
        public void setApellido(String apellido) { this.apellido = apellido; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getTelefono() { return telefono; }
        public void setTelefono(String telefono) { this.telefono = telefono; }
        public Rol getRol() { return rol; }
        public void setRol(Rol rol) { this.rol = rol; }
    }

    public static class AuthResponse {
        private String token;
        private String tipo = "Bearer";
        private Long id;
        private String nombre;
        private String apellido;
        private String email;
        private Rol rol;

        public AuthResponse() {}
        public AuthResponse(String token, Long id, String nombre, String apellido, String email, Rol rol) {
            this.token = token;
            this.tipo = "Bearer";
            this.id = id;
            this.nombre = nombre;
            this.apellido = apellido;
            this.email = email;
            this.rol = rol;
        }

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getApellido() { return apellido; }
        public void setApellido(String apellido) { this.apellido = apellido; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public Rol getRol() { return rol; }
        public void setRol(Rol rol) { this.rol = rol; }
    }

    public static class UsuarioDto {
        private Long id;
        private String nombre;
        private String apellido;
        private String email;
        private String telefono;
        private Rol rol;
        private boolean activo;

        public UsuarioDto() {}
        public UsuarioDto(Long id, String nombre, String apellido, String email, String telefono, Rol rol, boolean activo) {
            this.id = id;
            this.nombre = nombre;
            this.apellido = apellido;
            this.email = email;
            this.telefono = telefono;
            this.rol = rol;
            this.activo = activo;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getApellido() { return apellido; }
        public void setApellido(String apellido) { this.apellido = apellido; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getTelefono() { return telefono; }
        public void setTelefono(String telefono) { this.telefono = telefono; }
        public Rol getRol() { return rol; }
        public void setRol(Rol rol) { this.rol = rol; }
        public boolean isActivo() { return activo; }
        public void setActivo(boolean activo) { this.activo = activo; }
    }
}
