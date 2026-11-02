package com.foro.user_service.dto;

import com.foro.user_service.model.Rol;
import com.foro.user_service.model.Usuario;

import java.time.LocalDateTime;

// Respuesta sin la contraseña
public record UsuarioResponse(
        Long id,
        String username,
        String email,
        String nombre,
        Rol rol,
        LocalDateTime fechaRegistro
) {
    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getRol(),
                usuario.getFechaRegistro());
    }
}
