package ru.shvalieva.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "agent")
public class AgentProperties {
    private String serverUrl;
    private String token;
    private String hostId;
    private long intervalMs;
}