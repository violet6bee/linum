package ru.shvalieva.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.shvalieva.dto.AgentDataDto;
import ru.shvalieva.service.AgentDataService;

@RestController
@RequestMapping("/api/v1/data")
@RequiredArgsConstructor
@Slf4j
public class DataController {

    private final AgentDataService agentDataService;

    @PostMapping
    public ResponseEntity<String> receiveAgentData(@RequestBody AgentDataDto data, HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        log.info("Данные получены от агента {} с IP {}", data.getHostId(), ipAddress);
        agentDataService.processAgentData(data, ipAddress);
        return ResponseEntity.ok("Данные получены");
    }
}