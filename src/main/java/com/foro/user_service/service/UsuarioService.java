package com.foro.user_service.service;

import com.foro.user_service.dto.LoginRequest;
import com.foro.user_service.dto.UsuarioRequest;
import com.foro.user_service.dto.UsuarioResponse;
import com.foro.user_service.model.Rol;

import java.util.List;

public interface UsuarioService {

    List<UsuarioResponse> obtenerTodos();

    UsuarioResponse obtenerPorId(Long id);

    UsuarioResponse crear(UsuarioRequest request);

    UsuarioResponse actualizar(Long id, UsuarioRequest request);

    void eliminar(Long id);

    UsuarioResponse login(LoginRequest request);

    List<UsuarioResponse> obtenerPorRol(Rol rol);

    List<UsuarioResponse> buscarPorNombre(String nombre);
}
