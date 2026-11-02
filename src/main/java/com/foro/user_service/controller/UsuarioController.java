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

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(request));
    }

    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest request) {
        return usuarioService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
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
