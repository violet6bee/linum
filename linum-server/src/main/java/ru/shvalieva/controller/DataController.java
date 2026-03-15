package ru.shvalieva.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.shvalieva.dto.AgentDataDto;

@RestController
@RequestMapping("/api/v1/data")
public class DataController {

    @PostMapping
    public ResponseEntity<String> receiveAgentData(@RequestBody AgentDataDto data,
                                                   @RequestHeader("Authorization") String authHeader) {

        System.out.println("Received data from agent: " + data);
        return ResponseEntity.ok("Data received");
    }
}