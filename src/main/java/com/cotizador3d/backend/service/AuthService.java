package com.cotizador3d.backend.service;

import com.cotizador3d.backend.auth.AuthResponse;
import com.cotizador3d.backend.auth.LoginRequest;
import com.cotizador3d.backend.auth.RegisterRequest;
import com.cotizador3d.backend.model.Role;
import com.cotizador3d.backend.model.User;
import com.cotizador3d.backend.repository.UserRepository;
import com.cotizador3d.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : Role.CLIENTE)
                .build();

        repository.save(user);

        // Generate Token with extra claims such as role and userId
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole());
        extraClaims.put("userId", user.getId());

        var jwtToken = jwtService.generateToken(extraClaims, user);

        return AuthResponse.builder()
                .token(jwtToken)
                .id(user.getId())
                .role(user.getRole())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), // Treating email as the username for login
                        request.getPassword()));
        var user = repository.findByUsername(request.getEmail())
                .or(() -> repository.findAll().stream().filter(u -> u.getEmail().equals(request.getEmail()))
                        .findFirst())
                .orElseThrow();

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole());
        extraClaims.put("userId", user.getId());

        var jwtToken = jwtService.generateToken(extraClaims, user);

        return AuthResponse.builder()
                .token(jwtToken)
                .id(user.getId())
                .role(user.getRole())
                .build();
    }
}
