package com.foro.user_service.controller;

import com.foro.user_service.dto.LoginRequest;
import com.foro.user_service.dto.UsuarioRequest;
import com.foro.user_service.dto.UsuarioResponse;
import com.foro.user_service.model.Rol;
import com.foro.user_service.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    // Header con el id del usuario que realiza la operacion (el que inicio sesion)
    public static final String USUARIO_HEADER = "X-Usuario-Id";

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioResponse> obtenerTodos() {
        return usuarioService.obtenerTodos();
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtenerPorId(@PathVariable Long id) {
        return usuarioService.obtenerPorId(id);
    }

    // Registro: X-Usuario-Id es opcional y solo se necesita para crear un ADMIN (debe ser otro ADMIN)
    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest request,
                                                 @RequestHeader(value = USUARIO_HEADER, required = false) Long actorId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(request, actorId));
    }

    // Solo el propio usuario o un ADMIN
    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest request,
                                      @RequestHeader(USUARIO_HEADER) Long actorId) {
        return usuarioService.actualizar(id, request, actorId);
    }

    // Solo un ADMIN
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @RequestHeader(USUARIO_HEADER) Long actorId) {
        usuarioService.eliminar(id, actorId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public UsuarioResponse login(@Valid @RequestBody LoginRequest request) {
        return usuarioService.login(request);
    }

    @GetMapping("/rol/{rol}")
    public List<UsuarioResponse> obtenerPorRol(@PathVariable Rol rol) {
        return usuarioService.obtenerPorRol(rol);
    }

    @GetMapping("/buscar")
    public List<UsuarioResponse> buscarPorNombre(@RequestParam String nombre) {
        return usuarioService.buscarPorNombre(nombre);
    }
}
