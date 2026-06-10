package com.elrey.backend;

import com.elrey.backend.dto.LoginRequest;
import com.elrey.backend.dto.RegisterRequest;
import com.elrey.backend.dto.StartGameRequest;
import com.elrey.backend.repository.UserRepository;
import com.elrey.backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ElReyHaMuertoBackendApplicationTests {

	@Autowired MockMvc mockMvc;
	@Autowired ObjectMapper objectMapper;
	@Autowired UserService userService;
	@Autowired UserRepository userRepository;

	@BeforeEach
	void limpiarUsuarioTest() {
		userRepository.findByEmail("test@reino.es").ifPresent(u -> userRepository.delete(u));
		userRepository.findByEmail("integ@reino.es").ifPresent(u -> userRepository.delete(u));
	}

	@Test
	void contextLoads() {
		// Verifica que el contexto Spring arranca sin errores
	}

	@Test
	void registro_y_login_flujo_completo() throws Exception {
		RegisterRequest register = new RegisterRequest("ReyTest", "test@reino.es", "password123");

		mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(register)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("ReyTest"))
				.andExpect(jsonPath("$.email").value("test@reino.es"))
				.andExpect(jsonPath("$.token").isNotEmpty());

		LoginRequest login = new LoginRequest("test@reino.es", "password123");

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(login)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("ReyTest"))
				.andExpect(jsonPath("$.email").value("test@reino.es"))
				.andExpect(jsonPath("$.token").isNotEmpty());
	}

	@Test
	void login_con_password_incorrecto_devuelve_401() throws Exception {
		userService.register(new RegisterRequest("ReyTest", "test@reino.es", "password123"));

		LoginRequest login = new LoginRequest("test@reino.es", "wrongPassword");

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(login)))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("Credenciales incorrectas"));
	}

	@Test
	void registro_con_email_duplicado_devuelve_400() throws Exception {
		userService.register(new RegisterRequest("ReyTest", "test@reino.es", "password123"));

		RegisterRequest duplicado = new RegisterRequest("OtroRey", "test@reino.es", "otrapass");

		mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(duplicado)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("El correo ya está registrado en el reino"));
	}

	@Test
	void inicio_partida_requiere_autenticacion_devuelve_401_sin_token() throws Exception {
		mockMvc.perform(post("/api/game/start")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new StartGameRequest("ReyIntegración"))))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void inicio_partida_con_token_valido_devuelve_estado_inicial() throws Exception {
		// Registro para obtener un token real
		RegisterRequest register = new RegisterRequest("ReyInteg", "integ@reino.es", "pass123");
		String body = mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(register)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		String token = objectMapper.readTree(body).get("token").asText();

		mockMvc.perform(post("/api/game/start")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new StartGameRequest("ReyInteg"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.playerName").value("ReyInteg"))
				.andExpect(jsonPath("$.day").value(1))
				.andExpect(jsonPath("$.isAlive").value(true))
				.andExpect(jsonPath("$.stats.hygiene").value(100))
				.andExpect(jsonPath("$.stats.hunger").value(100))
				.andExpect(jsonPath("$.stats.popularity").value(50))
				.andExpect(jsonPath("$.stats.wealth").value(100));
	}
}
