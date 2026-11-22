package com.foro.user_service.service;

import com.foro.user_service.dto.LoginRequest;
import com.foro.user_service.dto.UsuarioRequest;
import com.foro.user_service.dto.UsuarioResponse;
import com.foro.user_service.model.Rol;

import java.util.List;

public interface UsuarioService {

    List<UsuarioResponse> obtenerTodos();

    UsuarioResponse obtenerPorId(Long id);

    // actorId: usuario que realiza la operacion (header X-Usuario-Id); null en un registro publico
    UsuarioResponse crear(UsuarioRequest request, Long actorId);

    UsuarioResponse actualizar(Long id, UsuarioRequest request, Long actorId);

    void eliminar(Long id, Long actorId);

    UsuarioResponse login(LoginRequest request);

    List<UsuarioResponse> obtenerPorRol(Rol rol);

    List<UsuarioResponse> buscarPorNombre(String nombre);
}
