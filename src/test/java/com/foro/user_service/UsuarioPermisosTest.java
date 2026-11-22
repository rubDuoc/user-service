package com.foro.user_service;

import com.foro.user_service.dto.UsuarioRequest;
import com.foro.user_service.exception.AccesoDenegadoException;
import com.foro.user_service.model.Rol;
import com.foro.user_service.model.Usuario;
import com.foro.user_service.repository.UsuarioRepository;
import com.foro.user_service.service.UsuarioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Reglas de privilegios por rol (sin base de datos: el repositorio es un mock)
class UsuarioPermisosTest {

    private UsuarioRepository repository;
    private UsuarioServiceImpl service;

    private final Usuario admin = usuario(1L, "admin_foro", Rol.ADMIN);
    private final Usuario juan = usuario(4L, "juan_gamer", Rol.USUARIO);
    private final Usuario maria = usuario(3L, "maria_dev", Rol.USUARIO);

    @BeforeEach
    void preparar() {
        repository = mock(UsuarioRepository.class);
        service = new UsuarioServiceImpl(repository, new BCryptPasswordEncoder());
        when(repository.findById(1L)).thenReturn(Optional.of(admin));
        when(repository.findById(4L)).thenReturn(Optional.of(juan));
        when(repository.findById(3L)).thenReturn(Optional.of(maria));
        when(repository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void registroPublicoQuedaComoUsuario() {
        assertThat(service.crear(request("nuevo", null), null).rol()).isEqualTo(Rol.USUARIO);
    }

    @Test
    void soloUnAdminPuedeCrearOtroAdmin() {
        assertThatThrownBy(() -> service.crear(request("nuevo", Rol.ADMIN), null))
                .isInstanceOf(AccesoDenegadoException.class);
        assertThatThrownBy(() -> service.crear(request("nuevo", Rol.ADMIN), 4L))
                .isInstanceOf(AccesoDenegadoException.class);
        assertThat(service.crear(request("nuevo", Rol.ADMIN), 1L).rol()).isEqualTo(Rol.ADMIN);
    }

    @Test
    void unUsuarioSoloEditaSuPropioPerfil() {
        assertThat(service.actualizar(4L, request("juan_gamer", null), 4L).username()).isEqualTo("juan_gamer");
        assertThatThrownBy(() -> service.actualizar(3L, request("maria_dev", null), 4L))
                .isInstanceOf(AccesoDenegadoException.class);
    }

    @Test
    void unUsuarioNoPuedeSubirseARolAdmin() {
        assertThatThrownBy(() -> service.actualizar(4L, request("juan_gamer", Rol.ADMIN), 4L))
                .isInstanceOf(AccesoDenegadoException.class);
        assertThat(service.actualizar(4L, request("juan_gamer", Rol.ADMIN), 1L).rol()).isEqualTo(Rol.ADMIN);
    }

    @Test
    void soloUnAdminEliminaUsuarios() {
        assertThatThrownBy(() -> service.eliminar(3L, 4L)).isInstanceOf(AccesoDenegadoException.class);
        verify(repository, never()).delete(any());
        service.eliminar(3L, 1L);
        verify(repository).delete(maria);
    }

    @Test
    void unActorInexistenteNoTienePermisos() {
        assertThatThrownBy(() -> service.eliminar(3L, 999L)).isInstanceOf(AccesoDenegadoException.class);
    }

    private static UsuarioRequest request(String username, Rol rol) {
        return new UsuarioRequest(username, username + "@correo.cl", "Clave1234", "Nombre", rol);
    }

    private static Usuario usuario(Long id, String username, Rol rol) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setUsername(username);
        u.setEmail(username + "@correo.cl");
        u.setNombre(username);
        u.setPassword("hash");
        u.setRol(rol);
        return u;
    }
}
