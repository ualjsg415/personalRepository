package com.elrey.backend.service;

import com.elrey.backend.dto.AuthResponse;
import com.elrey.backend.dto.LoginRequest;
import com.elrey.backend.dto.RegisterRequest;
import com.elrey.backend.entity.User;
import com.elrey.backend.repository.UserRepository;
import com.elrey.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new IllegalArgumentException("El correo ya está registrado en el reino");
        }
        if (userRepository.existsByUsername(req.username())) {
            throw new IllegalArgumentException("Ese nombre de soberano ya existe");
        }

        User user = new User();
        user.setUsername(req.username());
        user.setEmail(req.email());
        user.setPasswordHash(passwordEncoder.encode(req.password()));

        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved.getUsername(), saved.getId());
        return new AuthResponse(saved.getId(), saved.getUsername(), saved.getEmail(), token);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
            .orElseThrow(() -> new IllegalArgumentException("Credenciales incorrectas"));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Credenciales incorrectas");
        }

        String token = jwtService.generateToken(user.getUsername(), user.getId());
        return new AuthResponse(user.getId(), user.getUsername(), user.getEmail(), token);
    }
}
