package com.foro.user_service.service;

import com.foro.user_service.dto.LoginRequest;
import com.foro.user_service.dto.UsuarioRequest;
import com.foro.user_service.dto.UsuarioResponse;
import com.foro.user_service.exception.ConflictoException;
import com.foro.user_service.exception.CredencialesInvalidasException;
import com.foro.user_service.exception.RecursoNoEncontradoException;
import com.foro.user_service.model.Rol;
import com.foro.user_service.model.Usuario;
import com.foro.user_service.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<UsuarioResponse> obtenerTodos() {
        return usuarioRepository.findAll().stream().map(UsuarioResponse::from).toList();
    }

    @Override
    public UsuarioResponse obtenerPorId(Long id) {
        return UsuarioResponse.from(buscarUsuario(id));
    }

    @Override
    public UsuarioResponse crear(UsuarioRequest request) {
        if (usuarioRepository.existsByUsername(request.username())) {
            throw new ConflictoException("El username '" + request.username() + "' ya esta registrado");
        }
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new ConflictoException("El email '" + request.email() + "' ya esta registrado");
        }
        Usuario usuario = new Usuario();
        copiarDatos(request, usuario);
        usuario.setFechaRegistro(LocalDateTime.now());
        Usuario guardado = usuarioRepository.save(usuario);
        log.info("Usuario creado: id={}, username={}, rol={}", guardado.getId(), guardado.getUsername(), guardado.getRol());
        return UsuarioResponse.from(guardado);
    }

    @Override
    public UsuarioResponse actualizar(Long id, UsuarioRequest request) {
        Usuario usuario = buscarUsuario(id);
        if (!usuario.getUsername().equals(request.username()) && usuarioRepository.existsByUsername(request.username())) {
            throw new ConflictoException("El username '" + request.username() + "' ya esta registrado");
        }
        if (!usuario.getEmail().equals(request.email()) && usuarioRepository.existsByEmail(request.email())) {
            throw new ConflictoException("El email '" + request.email() + "' ya esta registrado");
        }
        copiarDatos(request, usuario);
        log.info("Usuario actualizado: id={}", id);
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    @Override
    public void eliminar(Long id) {
        usuarioRepository.delete(buscarUsuario(id));
        log.info("Usuario eliminado: id={}", id);
    }

    @Override
    public UsuarioResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByUsername(request.username())
                .filter(u -> passwordEncoder.matches(request.password(), u.getPassword()))
                .orElseThrow(() -> {
                    log.warn("Intento de login fallido para username={}", request.username());
                    return new CredencialesInvalidasException();
                });
        log.info("Login exitoso: username={}, rol={}", usuario.getUsername(), usuario.getRol());
        return UsuarioResponse.from(usuario);
    }

    @Override
    public List<UsuarioResponse> obtenerPorRol(Rol rol) {
        return usuarioRepository.findByRol(rol).stream().map(UsuarioResponse::from).toList();
    }

    @Override
    public List<UsuarioResponse> buscarPorNombre(String nombre) {
        return usuarioRepository.findByNombreContainingIgnoreCase(nombre).stream().map(UsuarioResponse::from).toList();
    }

    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un usuario con id " + id));
    }

    private void copiarDatos(UsuarioRequest request, Usuario usuario) {
        usuario.setUsername(request.username());
        usuario.setEmail(request.email());
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setNombre(request.nombre());
        usuario.setRol(request.rol());
    }
}
