package com.elrey.backend.service;

import com.elrey.backend.dto.AuthResponse;
import com.elrey.backend.dto.LoginRequest;
import com.elrey.backend.dto.RegisterRequest;
import com.elrey.backend.entity.User;
import com.elrey.backend.repository.UserRepository;
import com.elrey.backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock JwtService jwtService;
    @Spy  BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks UserService userService;

    @BeforeEach
    void stubJwt() {
        lenient().when(jwtService.generateToken(anyString(), anyLong())).thenReturn("mock.jwt.token");
    }

    @Test
    void register_crea_usuario_correctamente() {
        RegisterRequest req = new RegisterRequest("Rufo", "rufo@reino.es", "password123");
        when(userRepository.existsByEmail("rufo@reino.es")).thenReturn(false);
        when(userRepository.existsByUsername("Rufo")).thenReturn(false);

        User saved = new User();
        saved.setId(1L);
        saved.setUsername("Rufo");
        saved.setEmail("rufo@reino.es");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        AuthResponse result = userService.register(req);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.username()).isEqualTo("Rufo");
        assertThat(result.email()).isEqualTo("rufo@reino.es");
        assertThat(result.token()).isEqualTo("mock.jwt.token");
    }

    @Test
    void register_guarda_password_como_hash_bcrypt() {
        RegisterRequest req = new RegisterRequest("Rufo", "rufo@reino.es", "password123");
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByUsername(any())).thenReturn(false);

        User saved = new User();
        saved.setId(1L);
        saved.setUsername("Rufo");
        saved.setEmail("rufo@reino.es");

        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            assertThat(u.getPasswordHash()).isNotEqualTo("password123");
            assertThat(u.getPasswordHash()).startsWith("$2a$");
            return saved;
        });

        userService.register(req);
    }

    @Test
    void register_lanza_excepcion_si_email_duplicado() {
        RegisterRequest req = new RegisterRequest("Rufo2", "rufo@reino.es", "pass");
        when(userRepository.existsByEmail("rufo@reino.es")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("correo ya está registrado");

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_lanza_excepcion_si_username_duplicado() {
        RegisterRequest req = new RegisterRequest("Rufo", "nuevo@reino.es", "pass");
        when(userRepository.existsByEmail("nuevo@reino.es")).thenReturn(false);
        when(userRepository.existsByUsername("Rufo")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("soberano ya existe");

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_exitoso_devuelve_auth_response() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        User user = new User();
        user.setId(1L);
        user.setUsername("Rufo");
        user.setEmail("rufo@reino.es");
        user.setPasswordHash(encoder.encode("password123"));

        when(userRepository.findByEmail("rufo@reino.es")).thenReturn(Optional.of(user));

        AuthResponse result = userService.login(new LoginRequest("rufo@reino.es", "password123"));

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.username()).isEqualTo("Rufo");
        assertThat(result.email()).isEqualTo("rufo@reino.es");
        assertThat(result.token()).isEqualTo("mock.jwt.token");
    }

    @Test
    void login_lanza_excepcion_si_email_no_existe() {
        when(userRepository.findByEmail("noexiste@reino.es")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(new LoginRequest("noexiste@reino.es", "pass")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Credenciales incorrectas");
    }

    @Test
    void login_lanza_excepcion_si_password_incorrecto() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        User user = new User();
        user.setId(1L);
        user.setUsername("Rufo");
        user.setEmail("rufo@reino.es");
        user.setPasswordHash(encoder.encode("correctPassword"));

        when(userRepository.findByEmail("rufo@reino.es")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.login(new LoginRequest("rufo@reino.es", "wrongPassword")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Credenciales incorrectas");
    }
}
