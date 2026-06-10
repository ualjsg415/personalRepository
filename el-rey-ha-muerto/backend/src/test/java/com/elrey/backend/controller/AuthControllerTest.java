package com.elrey.backend.controller;

import com.elrey.backend.dto.*;
import com.elrey.backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    value = AuthController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean UserService userService;

    @Test
    void POST_register_exitoso_devuelve_200_con_datos_usuario() throws Exception {
        RegisterRequest req = new RegisterRequest("Rufo", "rufo@reino.es", "password123");
        AuthResponse resp = new AuthResponse(1L, "Rufo", "rufo@reino.es", "mock.jwt.token");
        when(userService.register(req)).thenReturn(resp);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("Rufo"))
                .andExpect(jsonPath("$.email").value("rufo@reino.es"))
                .andExpect(jsonPath("$.token").value("mock.jwt.token"));
    }

    @Test
    void POST_register_email_duplicado_devuelve_400() throws Exception {
        RegisterRequest req = new RegisterRequest("Rufo", "rufo@reino.es", "pass");
        when(userService.register(req))
                .thenThrow(new IllegalArgumentException("El correo ya está registrado en el reino"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El correo ya está registrado en el reino"));
    }

    @Test
    void POST_register_username_duplicado_devuelve_400() throws Exception {
        RegisterRequest req = new RegisterRequest("Rufo", "otro@reino.es", "pass");
        when(userService.register(req))
                .thenThrow(new IllegalArgumentException("Ese nombre de soberano ya existe"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ese nombre de soberano ya existe"));
    }

    @Test
    void POST_login_exitoso_devuelve_200_con_datos_usuario() throws Exception {
        LoginRequest req = new LoginRequest("rufo@reino.es", "password123");
        AuthResponse resp = new AuthResponse(1L, "Rufo", "rufo@reino.es", "mock.jwt.token");
        when(userService.login(req)).thenReturn(resp);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Rufo"))
                .andExpect(jsonPath("$.email").value("rufo@reino.es"))
                .andExpect(jsonPath("$.token").value("mock.jwt.token"));
    }

    @Test
    void POST_login_credenciales_incorrectas_devuelve_401() throws Exception {
        LoginRequest req = new LoginRequest("rufo@reino.es", "wrongPassword");
        when(userService.login(req))
                .thenThrow(new IllegalArgumentException("Credenciales incorrectas"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciales incorrectas"));
    }

    @Test
    void POST_login_email_inexistente_devuelve_401() throws Exception {
        LoginRequest req = new LoginRequest("noexiste@reino.es", "pass");
        when(userService.login(req))
                .thenThrow(new IllegalArgumentException("Credenciales incorrectas"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciales incorrectas"));
    }
}
