package ru.shvalieva.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.shvalieva.model.Host;
import ru.shvalieva.repository.HostRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/agents")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    private final HostRepository hostRepository;

    @PostMapping
    public ResponseEntity<?> createAgent(@RequestBody Map<String, String> payload) {
        log.info("Получен запрос на создание агента: {}", payload);
        String hostName = payload.get("name");
        if (hostName == null || hostName.isBlank()) {
            log.warn("Имя хоста отсутствует в запросе");
            return ResponseEntity.badRequest().body(Map.of("error", "Имя хоста обязательно"));
        }
        String token = UUID.randomUUID().toString();
        Host host = new Host();
        host.setToken(token);
        host.setName(hostName);
        host.setFirstSeen(null);
        hostRepository.save(host);
        log.info("Создан агент: имя={}, токен={}", hostName, token);
        return ResponseEntity.ok(Map.of(
                "token", token,
                "message", "Агент создан",
                "exampleConfig", "agent:\n  token: \"" + token + "\"\n  host-id: \"" + hostName + "\""
        ));
    }

    @GetMapping
    public ResponseEntity<List<Host>> getAllAgents() {
        return ResponseEntity.ok(hostRepository.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAgent(@PathVariable UUID id) {
        if (hostRepository.existsById(id)) {
            hostRepository.deleteById(id);
            log.info("Удалён агент id={}", id);
            return ResponseEntity.ok(Map.of("message", "Агент удалён"));
        }
        return ResponseEntity.notFound().build();
    }
}