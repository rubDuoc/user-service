package com.foro.user_service.service;

import com.foro.user_service.dto.LoginRequest;
import com.foro.user_service.dto.UsuarioRequest;
import com.foro.user_service.dto.UsuarioResponse;
import com.foro.user_service.exception.AccesoDenegadoException;
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

    // Registro publico: siempre como USUARIO. Para crear un ADMIN, quien lo crea debe ser ADMIN.
    @Override
    public UsuarioResponse crear(UsuarioRequest request, Long actorId) {
        Rol rol = request.rol() == null ? Rol.USUARIO : request.rol();
        if (rol == Rol.ADMIN && (actorId == null || !esAdmin(buscarActor(actorId)))) {
            throw new AccesoDenegadoException("Solo un administrador puede crear usuarios con rol ADMIN");
        }
        if (usuarioRepository.existsByUsername(request.username())) {
            throw new ConflictoException("El username '" + request.username() + "' ya esta registrado");
        }
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new ConflictoException("El email '" + request.email() + "' ya esta registrado");
        }
        Usuario usuario = new Usuario();
        copiarDatos(request, usuario);
        usuario.setRol(rol);
        usuario.setFechaRegistro(LocalDateTime.now());
        Usuario guardado = usuarioRepository.save(usuario);
        log.info("Usuario creado: id={}, username={}, rol={}", guardado.getId(), guardado.getUsername(), guardado.getRol());
        return UsuarioResponse.from(guardado);
    }

    // Cada usuario edita solo su propio perfil; un ADMIN puede editar a cualquiera y es el unico que cambia roles
    @Override
    public UsuarioResponse actualizar(Long id, UsuarioRequest request, Long actorId) {
        Usuario actor = buscarActor(actorId);
        Usuario usuario = buscarUsuario(id);
        if (!esAdmin(actor) && !actor.getId().equals(id)) {
            throw new AccesoDenegadoException("Solo puedes modificar tu propio perfil");
        }
        if (request.rol() != null && request.rol() != usuario.getRol() && !esAdmin(actor)) {
            throw new AccesoDenegadoException("Solo un administrador puede cambiar el rol de un usuario");
        }
        if (!usuario.getUsername().equals(request.username()) && usuarioRepository.existsByUsername(request.username())) {
            throw new ConflictoException("El username '" + request.username() + "' ya esta registrado");
        }
        if (!usuario.getEmail().equals(request.email()) && usuarioRepository.existsByEmail(request.email())) {
            throw new ConflictoException("El email '" + request.email() + "' ya esta registrado");
        }
        copiarDatos(request, usuario);
        if (request.rol() != null) {
            usuario.setRol(request.rol());
        }
        log.info("Usuario actualizado: id={}, por actorId={}", id, actorId);
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    // Solo un ADMIN puede eliminar usuarios
    @Override
    public void eliminar(Long id, Long actorId) {
        if (!esAdmin(buscarActor(actorId))) {
            throw new AccesoDenegadoException("Solo un administrador puede eliminar usuarios");
        }
        usuarioRepository.delete(buscarUsuario(id));
        log.info("Usuario eliminado: id={}, por actorId={}", id, actorId);
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

    // Usuario que realiza la operacion (header X-Usuario-Id): si no existe, no tiene permisos
    private Usuario buscarActor(Long actorId) {
        return usuarioRepository.findById(actorId)
                .orElseThrow(() -> new AccesoDenegadoException("El usuario " + actorId + " que realiza la operacion no existe"));
    }

    private boolean esAdmin(Usuario usuario) {
        return usuario.getRol() == Rol.ADMIN;
    }

    private void copiarDatos(UsuarioRequest request, Usuario usuario) {
        usuario.setUsername(request.username());
        usuario.setEmail(request.email());
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setNombre(request.nombre());
    }
}
