package ru.shvalieva.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.shvalieva.security.JwtUtil;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j

public class AuthController {
    private final JwtUtil jwtUtil;
    @Value("${admin.username}") private String adminUsername;
    @Value("${admin.password}") private String adminPassword;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> creds) {
        log.info("Получен POST /api/auth/login с параметрами: {}", creds);
        if (adminUsername.equals(creds.get("username")) && adminPassword.equals(creds.get("password"))) {
            return ResponseEntity.ok(Map.of("token", jwtUtil.generateToken(adminUsername)));
        }
        return ResponseEntity.status(401).body(Map.of("error", "Неверный логин или пароль"));
    }
}