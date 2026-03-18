package ru.shvalieva.controller;

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
    public ResponseEntity<String> receiveAgentData(@RequestBody AgentDataDto data) {

        log.info("Received data from agent: {}", data);
        agentDataService.processAgentData(data);
        return ResponseEntity.ok("Data received");
    }
}